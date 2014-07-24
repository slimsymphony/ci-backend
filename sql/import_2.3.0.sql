-- CONVERT BUILDGROUP URL CLOB TO VARCHAR2 --
alter table BUILD_GROUP add (temp VARCHAR2(4000 char));
update BUILD_GROUP set temp=DBMS_LOB.SUBSTR(url, 4000,1);
alter table BUILD_GROUP drop column url;
alter table BUILD_GROUP rename column temp to url;

-- Change column type from CLOB to VARCHAR(255) for URL in SLAVE_MACHINE --
alter table SLAVE_MACHINE add (URL_NEW VARCHAR(255) default '');
update SLAVE_MACHINE set URL_NEW=to_char(substr(URL,0,255));
alter table SLAVE_MACHINE drop column URL;
alter table SLAVE_MACHINE rename column URL_NEW to URL;

-- Deny build classification --
update SYS_USER set BUILDCLASSIFICATIONALLOWED = 0;
update SYS_USER set BUILDCLASSIFICATIONALLOWED = 1 where USERROLE='SYSTEM_ADMIN';