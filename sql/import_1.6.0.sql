-- Alter ciserver url column type from varchar to clob --
alter table S40CICORE.CISERVER add ( temp clob );
update S40CICORE.CISERVER set temp=url, url=null;
alter table S40CICORE.CISERVER drop column url;
alter table S40CICORE.CISERVER rename column temp to url;