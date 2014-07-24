-- REMOVE USER_GROUPS --
DROP TABLE SYS_USER_GROUP_GROUP;
DROP TABLE SYS_USER_GROUP CASCADE CONSTRAINTS;

-- UNIQUE WIDGETS PER USER --
ALTER TABLE WIDGET ADD CONSTRAINT uc_UserWidget UNIQUE (SYSUSER_ID, IDENTIFIER);

-- copy branch type to build group / to snapshot.
update BUILD_GROUP BG set BG.BRANCHTYPE = (select B.TYPE from JOB J left join BRANCH B on J.BRANCH_ID = B.ID where J.ID=BG.JOB_ID);

-- CREATE INDEX TO CHANGE COMMIT ID STRING. --
create index IDX_CH_COMMIT_ID on CHANGE(COMMITID);
-- CREATE INDEX TO CHANGE_FILE CHANGE_ID COLUMN --
create index IDX_CHF_CHANGE_ID on CHANGE_FILE(CHANGE_ID);

-- DROP BOOKMARKS --
DROP TABLE BOOKMARK;

---------------------------------------------------------------
-- MIGRATING BUILD_GROUP <-> CHANGE RELATION TO MANY-TO-MANY --
---------------------------------------------------------------
-- Remove changes with null commitid
delete from CHANGE where commitid is null;
-- Create copy of CHANGE table with ordered data
create table TEMP_CHANGE as select * from CHANGE order by COMMITID, CREATED desc;
-- Create primary key constraint for temp change
alter table TEMP_CHANGE add constraint TEMP_PK primary key (id);
-- Delete dublicate commitid rows from TEMP_CHANGE table.
delete from TEMP_CHANGE A where A.ROWID > any(select B.ROWID from TEMP_CHANGE B where A.COMMITID = B.COMMITID);
-- Update url information if exists
update TEMP_CHANGE TC set TC.URL = (select C.URL from CHANGE C where C.COMMITID = TC.COMMITID and C.URL is not null group by C.URL);
-- Create links to build groups
insert into BUILD_GROUP_CHANGE (BUILDGROUPS_ID, CHANGES_ID) select C.BUILDGROUP_ID, TC.ID from CHANGE C, TEMP_CHANGE TC where C.COMMITID = TC.COMMITID;
-- Update url information to CHANGE table
update CHANGE C set C.URL = (select TC.URL from TEMP_CHANGE TC where TC.ID = C.ID);
-- CREATE INDEX TO BUILD_GROUP_CHANGE CHANGES ID STRING. --
create index IDX_BGC_CHANGES_ID on BUILD_GROUP_CHANGE(CHANGES_ID);
-- CREATE INDEX TO BUILD_GROUP_CHANGE BUILD GROUPS ID STRING. --
create index IDX_BGC_BUILDGROUPS_ID on BUILD_GROUP_CHANGE(BUILDGROUPS_ID);
-- Delete change files for orphan changes
delete from CHANGE_FILE cf where not exists (select bgc.CHANGES_ID from BUILD_GROUP_CHANGE bgc where cf.CHANGE_ID = bgc.CHANGES_ID);
-- Delete orphan changes
delete from CHANGE c where not exists (select bgc.CHANGES_ID from BUILD_GROUP_CHANGE bgc where c.id = bgc.CHANGES_ID);
-- Drop temporal tables
drop table TEMP_CHANGE;
---------------------------------------------------------------

---------------------------------------------------------------
--        SET UP SECURITY ENTITIES TO USE PROJECT_ID         --
---------------------------------------------------------------

update JOB j set PROJECTID = (select b.PROJECT_ID from BRANCH b where b.ID = j.BRANCH_ID);

update BUILD_GROUP bg set PROJECTID = (select b.PROJECT_ID from JOB j left join BRANCH b on b.ID = j.BRANCH_ID where bg.JOB_ID = j.ID);

update CHANGE c set PROJECTID = (select b.PROJECT_ID from JOB j left join BRANCH b on b.ID = j.BRANCH_ID left join BUILD_GROUP bg ON j.ID = bg.JOB_ID where bg.ID = (select bgc.BUILDGROUPS_ID from BUILD_GROUP_CHANGE bgc where c.ID = bgc.CHANGES_ID and ROWNUM < 2));


-- Add disabled attribute for CIServer
update CISERVER set DISABLED = 0;

-- Remove BUILDGROUP_ID from CHANGE
alter table CHANGE drop column BUILDGROUP_ID;

-- Make commit id unique
ALTER TABLE CHANGE ADD CONSTRAINT COMMITID_UNIQUE UNIQUE (COMMITID);