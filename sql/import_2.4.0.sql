-- Move test result type to new table --
insert into VERIFICATION_TEST_RESULT_TYPES (VERIFICATION_ID, TESTRESULTTYPES)
    select ID, TESTRESULTTYPE from VERIFICATION WHERE TESTRESULTTYPE IS NOT NULL AND TESTRESULTTYPE != 'NONE';
alter table VERIFICATION drop column TESTRESULTTYPE;

insert into BVC_TEST_RESULT_TYPES (BVC_ID, TESTRESULTTYPES)
    select ID, TESTRESULTTYPE from BUILD_VERIFICATION_CONF WHERE TESTRESULTTYPE IS NOT NULL AND TESTRESULTTYPE != 'NONE';
alter table BUILD_VERIFICATION_CONF drop column TESTRESULTTYPE;

-- TRUNCATE ALL INCIDENTS, THEY ARE NOT HELPING ANYONE --
truncate table INCIDENT;

-- Change incident description to lob --
alter table INCIDENT modify (DESCRIPTION VARCHAR(4000 char));

-- Change classification types --
alter table BUILD_FAILURE modify (MESSAGE VARCHAR(4000 char));
alter table BUILD_FAILURE modify (TYPE VARCHAR(4000 char));
alter table BUILD_FAILURE modify (TESTCASENAME VARCHAR(4000 char));

-- CONVERT BUILDFAILURE REASON COLUMNS TO VARCHAR --
alter table BUILD_FAILURE_REASON add (temp VARCHAR2(4000 char));
update BUILD_FAILURE_REASON set temp=DBMS_LOB.SUBSTR(description, 4000,1);
alter table BUILD_FAILURE_REASON drop column description;
alter table BUILD_FAILURE_REASON rename column temp to description;

alter table BUILD_FAILURE_REASON add (temp VARCHAR2(4000 char));
update BUILD_FAILURE_REASON set temp=DBMS_LOB.SUBSTR(failcomment, 4000,1);
alter table BUILD_FAILURE_REASON drop column failcomment;
alter table BUILD_FAILURE_REASON rename column temp to failcomment;

-- Add more indexes for BUILD_GROUP table --
create index IDX_BG_STARTTIME on BUILD_GROUP(STARTTIME);
create index IDX_BG_ENDTIME on BUILD_GROUP(ENDTIME);

-- Add more indexes for BUILD table --
create index IDX_B_STARTTIME on BUILD(STARTTIME);
create index IDX_B_ENDTIME on BUILD(ENDTIME);

-- Add index for SLAVE_STAT_PER_LABEL table --
create index IDX_SSPL_PROVISIONTIME on SLAVE_STAT_PER_LABEL(PROVISIONTIME);

-- Add index for SLAVE_STAT_PER_MACHINE table --
create index IDX_SSPM_PROVISIONTIME on SLAVE_STAT_PER_MACHINE(PROVISIONTIME);

-- Add index for SLAVE_STAT_PER_POOL table --
create index IDX_SSPP_PROVISIONTIME on SLAVE_STAT_PER_POOL(PROVISIONTIME);

-- ZERO CI SERVER BUILDS RUNNING --
alter table CISERVER add BUILDSRUNNING number(10,0) DEFAULT 0 not null;
update CISERVER set BUILDSRUNNING = 0;

-- Add titles to report actions --
update REPORT_ACTION set title=status WHERE title IS NULL AND (status='SUCCESS' OR status='UNSTABLE' OR status='FAILURE');
update REPORT_ACTION set title='NOTIFY_UNSTABLE_MERGE' WHERE title is NULL AND status='STARTED';