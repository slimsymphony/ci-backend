-- DROP column that was forgot from 1.9.0 release migration --
alter table BUILD drop column BUILDVERIFICATIONCONF_ID;

update SYS_USER set SYSTEMMETRICSALLOWED = 0;
update SYS_USER set SYSTEMMETRICSALLOWED = 1 where USERROLE='SYSTEM_ADMIN';
update SYS_USER set SENDEMAIL = 1;
update SYS_USER set NEXTUSER = 1;

-- SET START NODE FALSE FOR ALL BUILDS --
update BUILD set STARTNODE = 0;

-- CREATE TEMP COLUMN FOR ROOT BUILDS TO BUILD TABLE --
alter table BUILD add ROOT_BUILD NUMBER(1,0) DEFAULT 0;

-- MARK ROOT BUILDS --
update BUILD set ROOT_BUILD = 1 where not (exists (select BUILD2.ID from BUILD_BUILD PARENT_BUILD, BUILD BUILD2 where PARENT_BUILD.CHILDBUILDS_ID = BUILD.ID and PARENT_BUILD.PARENTBUILDS_ID=BUILD2.ID));

-- COPY DATA FROM ROOT BUILD TO BUILD GROUP --
update BUILD_GROUP set (URL, PHASE, STATUS, STARTTIME, ENDTIME, JOBNAME, JOBDISPLAYNAME, GERRITREFSPEC, GERRITPATCHSETREVISION, GERRITURL, GERRITBRANCH, GERRITPROJECT) =
    (select URL, PHASE, STATUS, STARTTIME, ENDTIME, JOBNAME, JOBDISPLAYNAME, GERRITREFSPEC, GERRITPATCHSETREVISION, GERRITURL, GERRITBRANCH, GERRITPROJECT
        from BUILD left join BUILD_VERIFICATION_CONF on BUILD.ID = BUILD_VERIFICATION_CONF.BUILD_ID
            where ROOT_BUILD = 1 and BUILD.BUILDGROUP_ID = BUILD_GROUP.ID and ROWNUM < 2)
where BUILD_GROUP.ID IN (select BUILD_GROUP2.ID from BUILD_GROUP BUILD_GROUP2);

-- MAKE ALL ROOT BUILD CHILDS AS START NODES --
update BUILD set STARTNODE = 1 where ID in (select BUILD_BUILD.CHILDBUILDS_ID from BUILD left join BUILD_BUILD on BUILD.ID = BUILD_BUILD.PARENTBUILDS_ID
    where ROOT_BUILD = 1);

-- REMOVE BUILD_GROUP URLS (DO NOT WORK AFTER JENKINS HTTPS CHANGE)
update BUILD_GROUP set URL = null;

-- DROP UNUSED COLUMNS FROM BUILD_VERIFICATION_CONF --
alter table BUILD_VERIFICATION_CONF drop (GERRITREFSPEC, GERRITPATCHSETREVISION, GERRITURL, GERRITBRANCH, GERRITPROJECT);

-- DELETE ROOT BUILD ROWS FROM BUILD_BUILD TABLE --
delete from BUILD_BUILD where PARENTBUILDS_ID IN (select BUILD.ID from BUILD where ROOT_BUILD = 1);

-- DELETE BUILD INPUT PARAMETERS FOR BUILD_VERIFICATION_CONF OF ROOT BUILD --
delete from BUILD_INPUT_PARAM where BUILDVERIFICATIONCONF_ID IN
    (select CONF.ID from BUILD left join BUILD_VERIFICATION_CONF CONF on BUILD.ID = CONF.BUILD_ID where BUILD.ROOT_BUILD = 1 and CONF.ID is not null);

-- DELETE BUILD CUSTOM PARAMETERS FOR BUILD_VERIFICATION_CONF OF ROOT BUILD --
delete from BUILD_CUSTOM_PARAMETER where BUILDVERIFICATIONCONF_ID IN
    (select CONF.ID from BUILD left join BUILD_VERIFICATION_CONF CONF on BUILD.ID = CONF.BUILD_ID where BUILD.ROOT_BUILD = 1 and CONF.ID is not null);

-- DELETE BUILD RESULT DETAILS PARAMETERS FOR BUILD_VERIFICATION_CONF OF ROOT BUILD --
delete from BUILD_RESULT_DETAILS_PARAM where BUILDVERIFICATIONCONF_ID IN
    (select CONF.ID from BUILD left join BUILD_VERIFICATION_CONF CONF on BUILD.ID = CONF.BUILD_ID where BUILD.ROOT_BUILD = 1 and CONF.ID is not null);

-- DELETE BUILD_VERIFICATION_CONF FOR ROOT BUILDS --
delete from BUILD_VERIFICATION_CONF where BUILD_ID IN (select BUILD.ID from BUILD where ROOT_BUILD = 1);

-- DELETE BUILD EVENTS FOR ROOT BUILDS --
delete from BUILD_EVENT where BUILD_ID IN (select BUILD.ID from BUILD where ROOT_BUILD = 1);

-- DELETE ALL ROOT BUILDS --
delete from BUILD where ROOT_BUILD = 1;

-- DROP TEMP COLUMN --
alter table BUILD drop column ROOT_BUILD;

-- UPDATE ALL VERIFICATION TEST RESULT TYPES --
update VERIFICATION set TESTRESULTTYPE = 'NONE';
update BUILD_VERIFICATION_CONF set TESTRESULTTYPE = 'NONE';

-- CREATE SEQUENCE FOR CHANGES --
CREATE SEQUENCE CHANGE_SEQ MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 10 START WITH #CHANGE_THIS_WITH_REAL_VALUE# CACHE 20 NOORDER NOCYCLE;
-- CREATE SEQUENCE FOR CHANGE FILES --
CREATE SEQUENCE CHANGE_FILE_SEQ MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 50 START WITH #CHANGE_THIS_WITH_REAL_VALUE# CACHE 20 NOORDER NOCYCLE;
-- CREATE SEQUENCE FOR CHANGE TRACKERS --
CREATE SEQUENCE CHANGE_TRACKER_SEQ MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 10 START WITH #CHANGE_THIS_WITH_REAL_VALUE# CACHE 20 NOORDER NOCYCLE;

-- CREATE INDEX TO CHANGE_TRACKER COMMIT ID STRING.
create index IDX_CT_COMMIT_ID on CHANGE_TRACKER(COMMITID);

-- SET SLAVE_INSTANCE VERSION --
alter table SLAVE_INSTANCE add VERSION NUMBER(19,0) default 1 not null;

-- REMOVE USER_GROUPS --
DROP TABLE SYS_USER_GROUP_GROUP;
DROP TABLE SYS_USER_GROUP cascade constraints;