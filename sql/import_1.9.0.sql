-- SET DEFAULT VERIFICATION CONFIGURATION CARDINALITY --
update JOB_VERIFICATION_CONF set CARDINALITY = 'MANDATORY';
update CUSTOM_VERIFICATION_CONF set CARDINALITY = 'MANDATORY';
update BUILD_VERIFICATION_CONF set CARDINALITY = 'MANDATORY';

-- JOB DISABLED TO FALSE FOR ALL JOBS --
update JOB set DISABLED = 0;

-- SET DEFAULT JOB SCOPE AND TRIGGER --
update JOB set POLLINTERVAL = 5, TRIGGERSCOPE = 'USER', TRIGGERTYPE = 'MANUAL';

-- FOREIGN KEY INDEXING --

-- BOOKMARK TABLE --
create index IDX_BOOKMARK_JOB_ID on BOOKMARK(JOB_ID);
create index IDX_BOOKMARK_SYSUSER_ID on BOOKMARK(SYSUSER_ID);

-- BRANCH TABLE --
create index IDX_BR_PROJECT_ID on BRANCH(PROJECT_ID);

-- BRANCH_CISERVER TABLE --
create index IDX_BRC_BRANCHES_ID on BRANCH_CISERVER(BRANCHES_ID);
create index IDX_BRC_CISERVERS_ID on BRANCH_CISERVER(CISERVERS_ID);

-- BRANCH_VERIFICATION_CONF TABLE --
create index IDX_BRVC_VERIFICATION_ID on BRANCH_VERIFICATION_CONF(VERIFICATION_ID);
create index IDX_BRVC_BRANCH_ID on BRANCH_VERIFICATION_CONF(BRANCH_ID);
create index IDX_BRVC_PRODUCT_ID on BRANCH_VERIFICATION_CONF(PRODUCT_ID);

-- BUILD TABLE --
create index IDX_B_JOB_ID on BUILD(JOB_ID);
create index IDX_B_BUILDGROUP_ID on BUILD(BUILDGROUP_ID);

-- BUILD_BUILD TABLE --
create index IDX_BB_CHILDBUILDS_ID on BUILD_BUILD(CHILDBUILDS_ID);
create index IDX_BB_PARENTBUILDS_ID on BUILD_BUILD(PARENTBUILDS_ID);

-- BUILD_CUSTOM_PARAMETER TABLE --
create index IDX_BCP_BUILDVER_CONF_ID on BUILD_CUSTOM_PARAMETER(BUILDVERIFICATIONCONF_ID);

-- BUILD_GROUP TABLE --
create index IDX_BG_CISERVER_ID on BUILD_GROUP(CISERVER_ID);

-- BUILD_INPUT_PARAM TABLE --
create index IDX_BIP_BUILDVER_ID on BUILD_INPUT_PARAM(BUILDVERIFICATIONCONF_ID);

-- BUILD_VERIFICATION_CONF TABLE --
create index IDX_BVC_BUILD_ID on BUILD_VERIFICATION_CONF(BUILD_ID);

-- CHANGE TABLE --
create index IDX_CHANGE_BUILDGROUP_ID on CHANGE(BUILDGROUP_ID);

-- CHANGE_FILE TABLE
create index IDX_CHANGE_FILE_CHANGE_ID on CHANGE_FILE(CHANGE_ID);

-- CUSTOM_PARAM TABLE --
create index IDX_CP_VERIFICATION_ID on CUSTOM_PARAM(VERIFICATION_ID);

-- CUSTOM_PARAM_VALUE TABLE --
create index IDX_CPV_CUSTOMPARAM_ID on CUSTOM_PARAM_VALUE(CUSTOMPARAM_ID);

-- CUSTOM_VERIFICATION_CONF TABLE --
create index IDX_CVC_JOBCUSTOMVER_ID on CUSTOM_VERIFICATION_CONF(JOBCUSTOMVERIFICATION_ID);
create index IDX_CVC_PRODUCT_ID on CUSTOM_VERIFICATION_CONF(PRODUCT_ID);

-- CUSTOM_VERIFICATION_PARAM TABLE --
create index IDX_CVP_CUSTOMVER_ID on CUSTOM_VERIFICATION_PARAM(CUSTOMVERIFICATION_ID);
create index IDX_CVP_CUSTOMPARAM_ID on CUSTOM_VERIFICATION_PARAM(CUSTOMPARAM_ID);

-- INPUT_PARAM TABLE --
create index IDX_IP_VERIFICATION_ID on INPUT_PARAM(VERIFICATION_ID);

-- JOB TABLE --
create index IDX_J_BRANCH_ID on JOB(BRANCH_ID);
create index IDX_J_OWNER_ID on JOB(OWNER_ID);

-- JOB_ANNOUNCEMENT TABLE --
create index IDX_JA_JOB_ID on JOB_ANNOUNCEMENT(JOB_ID);

-- JOB_CUSTOM_VERIFICATION TABLE --
create index IDX_JCUV_JOB_ID on JOB_CUSTOM_VERIFICATION(JOB_ID);
create index IDX_JCUV_VERIFICATION_ID on JOB_CUSTOM_VERIFICATION(VERIFICATION_ID);

-- JOB_POST_VERIFICATION TABLE --
create index IDX_JPOV_JOB_ID on JOB_POST_VERIFICATION(JOB_ID);
create index IDX_JPOV_VERIFICATION_ID on JOB_POST_VERIFICATION(VERIFICATION_ID);

-- JOB_PRE_VERIFICATION TABLE --
create index IDX_JPREV_JOB_ID on JOB_PRE_VERIFICATION(JOB_ID);
create index IDX_JPREV_VERIFICATION_ID on JOB_PRE_VERIFICATION(VERIFICATION_ID);

-- JOB_VERIFICATION_CONF TABLE --
create index IDX_JVC_JOB_ID on JOB_VERIFICATION_CONF(JOB_ID);
create index IDX_JVC_VERIFICATION_ID on JOB_VERIFICATION_CONF(VERIFICATION_ID);
create index IDX_JVC_PRODUCT_ID on JOB_VERIFICATION_CONF(PRODUCT_ID);

-- PROJECT TABLE --
create index IDX_P_PROJECTGROUP_ID on PROJECT(PROJECTGROUP_ID);
create index IDX_P_GERRIT_ID on PROJECT(GERRIT_ID);

-- PROJECT_ANNOUNCEMENT TABLE --
create index IDX_PA_PROJECT_ID on PROJECT_ANNOUNCEMENT(PROJECT_ID);

-- PROJECT_EXTERNAL_LINK TABLE --
create index IDX_PEL_PROJECT_ID on PROJECT_EXTERNAL_LINK(PROJECT_ID);

-- PROJECT_PRODUCT TABLE --
create index IDX_PP_PRODUCTS_ID on PROJECT_PRODUCT(PRODUCTS_ID);
create index IDX_PP_PROJECTS_ID on PROJECT_PRODUCT(PROJECTS_ID);

-- PROJECT_VERIFICATION TABLE --
create index IDX_PV_PROJECTS_ID on PROJECT_VERIFICATION(PROJECTS_ID);
create index IDX_PV_VERIFICATIONS_ID on PROJECT_VERIFICATION(VERIFICATIONS_ID);

-- PROJECT_VERIFICATION_CONF TABLE --
create index IDX_PVC_PROJECT_ID on PROJECT_VERIFICATION_CONF(PROJECT_ID);
create index IDX_PVC_VERIFICATION_ID on PROJECT_VERIFICATION_CONF(VERIFICATION_ID);
create index IDX_PVC_PRODUCT_ID on PROJECT_VERIFICATION_CONF(PRODUCT_ID);

-- REPORT_ACTION TABLE --
create index IDX_RA_JOB_ID on REPORT_ACTION(JOB_ID);

-- SLAVE_SLAVELABEL TABLE --
create index IDX_SLSL_SLAVES_ID on SLAVE_SLAVELABEL(SLAVES_ID);
create index IDX_SLSL_SLAVELABELS_ID on SLAVE_SLAVELABEL(SLAVELABELS_ID);

-- SLAVE_SLAVEPOOL TABLE --
create index IDX_SLSP_SLAVEPOOLS_ID on SLAVE_SLAVEPOOL(SLAVEPOOLS_ID);
create index IDX_SLSP_SLAVES_ID on SLAVE_SLAVEPOOL(SLAVES_ID);

-- VERIFICATION_VERIFICATION TABLE --
create index IDX_VV_CHILDVERIFICATIONS_ID on VERIFICATION_VERIFICATION(CHILDVERIFICATIONS_ID);
create index IDX_VV_PARENTVERIFICATIONS_ID on VERIFICATION_VERIFICATION(PARENTVERIFICATIONS_ID);

-- REVERSE BUILD - BUILDVERIFICATION_CONF RELATION. BE WITH NEW ENTITY MODEL MUST BE DEPLOYED BEFORE RUNNING THIS. IT WILL CREATE NEEDED COLUMNS. --
update BUILD_VERIFICATION_CONF set (BUILD_ID) = (select ID from BUILD where BUILDVERIFICATIONCONF_ID = BUILD_VERIFICATION_CONF.ID);
alter table BUILD drop column BUILDVERIFICATIONCONF_ID;

-- END OF SQL FILE import_1.9.0.sql --