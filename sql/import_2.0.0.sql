-- SET ALL GERRITS TO LISTEN TO STREAMS --
update GERRIT set LISTENSTREAM = 1;

-- SET EMAIL_REPORT_ACTION SENDONLYIFSTATUSCHANGED TO FALSE --
update EMAIL_REPORT_ACTION set SENDONLYIFSTATUSCHANGED = 0;

-- REMOVE OLD STYLE USER GROUPS --
delete from SYS_USER_GROUP where CUSTOMGROUP = null;

-- COPY CI SERVER INFORMATION TO BUILDGROUP_CISERVERS --
insert into BUILD_GROUP_CISERVER (ID, URL, PORT, USERNAME, PASSWORD, BUILDGROUP_ID, CREATED) 
    select HIBERNATE_SEQUENCE.NEXTVAL, CISERVER.URL, CISERVER.PORT, CISERVER.USERNAME, CISERVER.PASSWORD, BUILD_GROUP.ID, CURRENT_TIMESTAMP 
    from BUILD_GROUP left join CISERVER on BUILD_GROUP.CISERVER_ID = CISERVER.ID;
alter table BUILD_GROUP drop column CISERVER_ID;

-- DELETE ALL (LEGACY) BUILDS WITH NULL BUILDGROUP_ID --
delete from BUILD where BUILDGROUP_ID is null;

-- CREATE JOB_ID COLUMN TO BUILD_GROUP TABLE --
alter table BUILD_GROUP add JOB_ID NUMBER(19,0);
alter table BUILD_GROUP add constraint fk_bg_job foreign key (JOB_ID) references JOB(id);
create index IDX_BG_JOB_ID on BUILD_GROUP(JOB_ID);

-- COPY JOB_ID FROM BUILD TO BUILD_GROUP --
update BUILD_GROUP set JOB_ID = (select JOB_ID from BUILD where BUILDGROUP_ID = BUILD_GROUP.ID group by JOB_ID);