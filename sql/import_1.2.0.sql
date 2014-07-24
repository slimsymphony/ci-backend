-- DELETES --

-- Delete old toolbox builds --
delete from S40CICORE.BUILD where JOB_ID = 1;
-- Delete old toolbox job --
delete from S40CICORE.JOB where ID = 1;
-- Delete project verification configurations where product gaia_ds or gaia_ss --
delete from S40CICORE.PROJECT_VERIFICATION_CONF where PRODUCT_ID = 1 or PRODUCT_ID = 2;
-- Delete project products gaia_ds and gaia_ss --
delete from S40CICORE.PROJECT_PRODUCT where PRODUCTS_ID = 1 or PRODUCTS_ID = 2;
-- Delete products gaia_ds and gaia_ss --
delete from S40CICORE.PRODUCT where ID = 1 or ID = 2;

-- UPDATES --

-- Update evo project to s40_ng_mw
update S40CICORE.PROJECT set "NAME" = 's40_ng_mw' where ID = 1;
-- Rename verification win_nose_no_ui to win_nose
update S40CICORE.VERIFICATION set "NAME" = 'win_nose' where ID = 9;

-- INSERTS --

-- Projects --
insert into S40CICORE.PROJECT (ID, "NAME") values (2, 's40_ng_ui');
-- Branches --
insert into S40CICORE.BRANCH (ID, "NAME", PROJECT_ID) values (4, 'toolbox', 2);
-- Products --
insert into S40CICORE.PRODUCT (ID, "NAME") values (5, 'yapas_standalone');
-- Verifications --
insert into S40CICORE.VERIFICATION (ID, "NAME") values (1, 'cppunit');
insert into S40CICORE.VERIFICATION (ID, "NAME") values (6, 'target_with_ui');
insert into S40CICORE.VERIFICATION (ID, "NAME") values (8, 'win_nose_with_ui');
insert into S40CICORE.VERIFICATION (ID, "NAME") values (10, 'nose_with_ui');
insert into S40CICORE.VERIFICATION (ID, "NAME") values (11, 'nose_mw_and_ui');
insert into S40CICORE.VERIFICATION (ID, "NAME") values (12, 'target_mw_and_ui');
insert into S40CICORE.VERIFICATION (ID, "NAME") values (13, 'win_nose_mw_and_ui');
insert into S40CICORE.VERIFICATION (ID, "NAME") values (14, 'linux');
insert into S40CICORE.VERIFICATION (ID, "NAME") values (15, 'win');
-- Project products for s40_ng_ui --
insert into S40CICORE.PROJECT_PRODUCT (PROJECTS_ID, PRODUCTS_ID) values (2, 3);
insert into S40CICORE.PROJECT_PRODUCT (PROJECTS_ID, PRODUCTS_ID) values (2, 4);
insert into S40CICORE.PROJECT_PRODUCT (PROJECTS_ID, PRODUCTS_ID) values (2, 5);
-- Project verifications for s40_ng_mw --
insert into S40CICORE.PROJECT_VERIFICATION (PROJECTS_ID, VERIFICATIONS_ID) values (1, 1);
insert into S40CICORE.PROJECT_VERIFICATION (PROJECTS_ID, VERIFICATIONS_ID) values (1, 6);
insert into S40CICORE.PROJECT_VERIFICATION (PROJECTS_ID, VERIFICATIONS_ID) values (1, 8);
insert into S40CICORE.PROJECT_VERIFICATION (PROJECTS_ID, VERIFICATIONS_ID) values (1, 10);
-- Project verifications for s40_ng_ui --
insert into S40CICORE.PROJECT_VERIFICATION (PROJECTS_ID, VERIFICATIONS_ID) values (2, 11);
insert into S40CICORE.PROJECT_VERIFICATION (PROJECTS_ID, VERIFICATIONS_ID) values (2, 12);
insert into S40CICORE.PROJECT_VERIFICATION (PROJECTS_ID, VERIFICATIONS_ID) values (2, 13);
insert into S40CICORE.PROJECT_VERIFICATION (PROJECTS_ID, VERIFICATIONS_ID) values (2, 14);
insert into S40CICORE.PROJECT_VERIFICATION (PROJECTS_ID, VERIFICATIONS_ID) values (2, 15);
-- Project verification configurations for s40_ng_mw --
insert into S40CICORE.PROJECT_VERIFICATION_CONF (ID, PROJECT_ID, PRODUCT_ID, VERIFICATION_ID) values (38, 1, 3, 1);
insert into S40CICORE.PROJECT_VERIFICATION_CONF (ID, PROJECT_ID, PRODUCT_ID, VERIFICATION_ID) values (39, 1, 3, 6);
insert into S40CICORE.PROJECT_VERIFICATION_CONF (ID, PROJECT_ID, PRODUCT_ID, VERIFICATION_ID) values (40, 1, 3, 8);
insert into S40CICORE.PROJECT_VERIFICATION_CONF (ID, PROJECT_ID, PRODUCT_ID, VERIFICATION_ID) values (41, 1, 3, 10);
insert into S40CICORE.PROJECT_VERIFICATION_CONF (ID, PROJECT_ID, PRODUCT_ID, VERIFICATION_ID) values (42, 1, 4, 1);
insert into S40CICORE.PROJECT_VERIFICATION_CONF (ID, PROJECT_ID, PRODUCT_ID, VERIFICATION_ID) values (43, 1, 4, 6);
insert into S40CICORE.PROJECT_VERIFICATION_CONF (ID, PROJECT_ID, PRODUCT_ID, VERIFICATION_ID) values (44, 1, 4, 8);
insert into S40CICORE.PROJECT_VERIFICATION_CONF (ID, PROJECT_ID, PRODUCT_ID, VERIFICATION_ID) values (45, 1, 4, 10);
-- Project verification configurations fro s40_ng_ui --
insert into S40CICORE.PROJECT_VERIFICATION_CONF (ID, PROJECT_ID, PRODUCT_ID, VERIFICATION_ID) values (46, 2, 3, 11);
insert into S40CICORE.PROJECT_VERIFICATION_CONF (ID, PROJECT_ID, PRODUCT_ID, VERIFICATION_ID) values (47, 2, 3, 12);
insert into S40CICORE.PROJECT_VERIFICATION_CONF (ID, PROJECT_ID, PRODUCT_ID, VERIFICATION_ID) values (48, 2, 3, 13);
insert into S40CICORE.PROJECT_VERIFICATION_CONF (ID, PROJECT_ID, PRODUCT_ID, VERIFICATION_ID) values (49, 2, 4, 11);
insert into S40CICORE.PROJECT_VERIFICATION_CONF (ID, PROJECT_ID, PRODUCT_ID, VERIFICATION_ID) values (50, 2, 4, 12);
insert into S40CICORE.PROJECT_VERIFICATION_CONF (ID, PROJECT_ID, PRODUCT_ID, VERIFICATION_ID) values (51, 2, 4, 13);
insert into S40CICORE.PROJECT_VERIFICATION_CONF (ID, PROJECT_ID, PRODUCT_ID, VERIFICATION_ID) values (52, 2, 5, 14);
insert into S40CICORE.PROJECT_VERIFICATION_CONF (ID, PROJECT_ID, PRODUCT_ID, VERIFICATION_ID) values (53, 2, 5, 15);