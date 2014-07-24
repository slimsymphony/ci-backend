-- SET default values for verification enums. --
update VERIFICATION set TYPE = 'NORMAL';
update VERIFICATION set PARENTSTATUSTHRESHOLD = 'NOT_BUILT';

-- DELETING default jobs with parent and builds --
delete from BUILD where JOB_ID in (select ID from JOB where PARENT_ID is not null and TYPE = 'DEFAULT');
delete from JOB where PARENT_ID is not null and TYPE = 'DEFAULT';

-- DELETING toolbox jobs with parent and builds --
delete from BUILD where JOB_ID in (select ID from JOB where TYPE = 'TOOLBOX');
delete from JOB where PARENT_ID is not null and TYPE = 'TOOLBOX';
delete from BUILD_CUSTOM_PARAMETER;
delete from BUILD_VERIFICATION_CONF;

-- DROP job parent id column --
alter table JOB drop column PARENT_ID;

-- UPDATE BUILD information from job table (display name, name, url) --
update BUILD set (JOBDISPLAYNAME, JOBNAME, JOBURL) = (select DISPLAYNAME, NAME, URL from JOB where JOB.ID = BUILD.JOB_ID);