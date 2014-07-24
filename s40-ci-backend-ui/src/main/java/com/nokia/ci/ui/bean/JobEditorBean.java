package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.BranchEJB;
import com.nokia.ci.ejb.CustomParamEJB;
import com.nokia.ci.ejb.CustomVerificationParamEJB;
import com.nokia.ci.ejb.JobCustomVerificationEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.ReportActionEJB;
import com.nokia.ci.ejb.TemplateCustomVerificationEJB;
import com.nokia.ci.ejb.TemplateEJB;
import com.nokia.ci.ejb.VerificationEJB;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.JobVerificationConfEJB;
import com.nokia.ci.ejb.CustomVerificationConfEJB;
import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.UserFileEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.exception.TasProductReadException;
import com.nokia.ci.ejb.exception.UnauthorizedException;
import com.nokia.ci.ejb.model.AccessScope;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.BranchType;
import com.nokia.ci.ejb.model.BranchVerificationConf;
import com.nokia.ci.ejb.model.CustomParam;
import com.nokia.ci.ejb.model.CustomParamValue;
import com.nokia.ci.ejb.model.CustomVerificationConf;
import com.nokia.ci.ejb.model.CustomVerificationParam;
import com.nokia.ci.ejb.model.EmailReportAction;
import com.nokia.ci.ejb.model.GerritReportAction;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.JobCustomParameter;
import com.nokia.ci.ejb.model.JobCustomVerification;
import com.nokia.ci.ejb.model.JobTriggerScope;
import com.nokia.ci.ejb.model.JobTriggerType;
import com.nokia.ci.ejb.model.JobVerificationConf;
import com.nokia.ci.ejb.model.NotificationReportAction;
import com.nokia.ci.ejb.model.Product;
import com.nokia.ci.ejb.model.ProjectVerificationConf;
import com.nokia.ci.ejb.model.ReportAction;
import com.nokia.ci.ejb.reportaction.ReportActionStatus;
import com.nokia.ci.ejb.model.RepositoryType;
import com.nokia.ci.ejb.model.StatusTriggerPattern;
import com.nokia.ci.ejb.model.FileTriggerPattern;
import com.nokia.ci.ejb.model.FileType;
import com.nokia.ci.ejb.model.OwnershipScope;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.reportaction.ReportActionTitle;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.model.Template;
import com.nokia.ci.ejb.model.TemplateCustomVerification;
import com.nokia.ci.ejb.model.TemplateCustomVerificationConf;
import com.nokia.ci.ejb.model.TemplateCustomVerificationParam;
import com.nokia.ci.ejb.model.TemplateVerificationConf;
import com.nokia.ci.ejb.model.Verification;
import com.nokia.ci.ejb.tas.TasDevice;
import com.nokia.ci.ejb.tas.TasDeviceFilter;
import com.nokia.ci.ejb.tas.TasReaderEJB;
import com.nokia.ci.ejb.util.RelationUtil;
import com.nokia.ci.ejb.util.TimezoneUtil;
import com.nokia.ci.ui.exception.QueryParamException;
import com.nokia.ci.ui.model.VerificationConfCell;
import com.nokia.ci.ui.model.VerificationConfRow;
import com.nokia.ci.ui.util.UIVerificationConfUtil;
import com.nokia.ci.ejb.model.UserFile;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.security.RolesAllowed;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.UploadedFile;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for editing Job.
 *
 * @author vrouvine
 */
@Named
@ViewScoped
public class JobEditorBean extends AbstractUIBaseBean {

    private static final Logger log = LoggerFactory.getLogger(JobEditorBean.class);
    private Long projectId;
    private List<VerificationConfRow> verificationConfRows;
    private List<Product> products;
    private List<Verification> verifications;
    private DualListModel<Verification> postBuildVerifications;
    private DualListModel<Verification> preBuildVerifications;
    private List<Branch> availableBranches;
    private Map<CustomVerificationParam, List<SelectItem>> customParamValueItemsMap;
    private JobCustomVerification selectedCustomVerification;
    private Branch selectedBranch;
    private Job job;
    private Project project;
    private List<ReportModel<GerritReportAction>> gerritReports = new ArrayList<ReportModel<GerritReportAction>>();
    private List<ReportModel<EmailReportAction>> emailReports = new ArrayList<ReportModel<EmailReportAction>>();
    private List<ReportModel<NotificationReportAction>> notificationReports = new ArrayList<ReportModel<NotificationReportAction>>();
    private StatusTriggerPattern newStatusTrigger;
    private List<StatusTriggerPattern> statusTriggerPatterns = new ArrayList<StatusTriggerPattern>();
    private FileTriggerPattern newFileTrigger;
    private List<FileTriggerPattern> fileTriggerPatterns = new ArrayList<FileTriggerPattern>();
    private JobCustomParameter customParameter;
    private VerificationConfCell selectedCellForOptions;
    private String selectedTasDevice;
    private List<TasDevice> matchingTasDevices;
    private List<TasDevice> allTasDevices;
    private String optionsFilter = "";
    private List<JobCustomParameter> customParameters = new ArrayList<JobCustomParameter>();
    private String selectedOwnershipScope;
    private String selectedAccessScope;
    private DualListModel<UserFile> userFileRelationDualList;
    @Inject
    private ReportActionEJB reportActionEJB;
    @Inject
    private ProjectEJB projectEJB;
    @Inject
    private JobEJB jobEJB;
    @Inject
    private BranchEJB branchEJB;
    @Inject
    private JobCustomVerificationEJB jobCustomVerificationEJB;
    @Inject
    private CustomVerificationParamEJB customVerificationParamEJB;
    @Inject
    private VerificationEJB verificationEJB;
    @Inject
    private CustomParamEJB customParamEJB;
    @Inject
    private TemplateEJB templateEJB;
    @Inject
    private TemplateCustomVerificationEJB templateCustomVerificationEJB;
    @Inject
    private TasReaderEJB tasReaderEJB;
    @Inject
    private HttpSessionBean httpSessionBean;
    @Inject
    private CustomVerificationConfEJB customVerificationConfEJB;
    @Inject
    private JobVerificationConfEJB jobVerificationConfEJB;
    @Inject
    private SysConfigEJB sysConfigEJB;
    @Inject
    private SysUserEJB sysUserEJB;
    @Inject
    private UserFileEJB userFileEJB;
    private CustomVerificationConf selectedCustomVerificationConf;
    private JobVerificationConf selectedJobVerificationConf;
    private VerificationConfRow selectedVerificationConfRow;

    /**
     * Creates a new instance of JobEditorBean
     */
    public JobEditorBean() {
    }

    @Override
    protected void init() throws BackendAppException, QueryParamException {
        String jobId = getQueryParam("verificationId");
        if (jobId == null) {
            job = new Job();
            job.setTriggerType(JobTriggerType.MANUAL);
            job.setTriggerScope(JobTriggerScope.USER);
            job.setPollInterval(5);
            job.setOwner(httpSessionBean.getSysUser());
        } else {
            log.debug("Finding job {} for editing!", jobId);
            job = jobEJB.readSecure(Long.parseLong(jobId));
            if (job.getTriggerScope() == null) {
                job.setTriggerScope(JobTriggerScope.USER);
            }
        }

        if (job.getBranch() != null && job.getBranch().getId() != null) {
            selectedBranch = branchEJB.read(job.getBranch().getId());
            projectId = selectedBranch.getProject().getId();
        }

        if (projectId == null) {
            String projectIdString = getMandatoryQueryParam("projectId");
            projectId = Long.parseLong(projectIdString);

            if (!httpSessionBean.hasAccessToProject(projectId)) {
                throw new UnauthorizedException();
            }
        }

        project = projectEJB.read(projectId);

        initProducts();
        initVerifications();
        initPrePostVerifications();
        initAvailableBranches();

        // Set draft branch as primary default if new job.
        if (selectedBranch == null) {
            for (Branch branch : availableBranches) {
                if (branch.getType() == BranchType.DRAFT) {
                    selectedBranch = branch;
                    break;
                }
            }
        }

        // Set toolbox branch as secondary default if new job.
        if (selectedBranch == null) {
            for (Branch branch : availableBranches) {
                if (branch.getType() == BranchType.TOOLBOX) {
                    selectedBranch = branch;
                    break;
                }
            }
            // Set the first branch as selected if no toolbox branch is
            // available
            if (selectedBranch == null && !availableBranches.isEmpty()) {
                selectedBranch = availableBranches.get(0);
            }

        }

        // If branch still null (no toolbox or draft branch available), then
        // select
        // first available branch from list as selected.
        if (selectedBranch == null && !availableBranches.isEmpty()) {
            selectedBranch = availableBranches.get(0);
        }

        initJobCustomVerifications();
        initVerificationConfRows();
        initReports();
        initStatusTriggerPatterns();
        initFileTriggerPatterns();
        initCustomParameters();
        updateUserFileRelation();
    }

    public Job getJob() {
        return job;
    }

    public List<ReportModel<GerritReportAction>> getGerritReports() {
        return gerritReports;
    }

    public List<ReportModel<EmailReportAction>> getEmailReports() {
        return emailReports;
    }

    public List<ReportModel<NotificationReportAction>> getNotificationReports() {
        return notificationReports;
    }

    public List<Product> getProducts() {
        return products;
    }

    public DualListModel<Verification> getPreBuildVerifications() {
        return preBuildVerifications;
    }

    public void setPreBuildVerifications(DualListModel<Verification> verifications) {
        this.preBuildVerifications = verifications;
    }

    public DualListModel<Verification> getPostBuildVerifications() {
        return postBuildVerifications;
    }

    public void setPostBuildVerifications(DualListModel<Verification> verifications) {
        this.postBuildVerifications = verifications;
    }

    public List<Verification> getVerifications() {
        return verifications;
    }

    public JobCustomVerification getSelectedCustomVerification() {
        return selectedCustomVerification;
    }

    public void setSelectedCustomVerification(JobCustomVerification selectedCustomVerification) {
        this.selectedCustomVerification = selectedCustomVerification;
    }

    public int getMinInt() {
        return Integer.MIN_VALUE;
    }

    public boolean isRepositoryTypeGit() {
        if (selectedBranch == null) {
            return false;
        }

        if (selectedBranch.getType().getRepositoryType() == RepositoryType.GIT) {
            return true;
        }

        return false;
    }

    public boolean isToolboxBranch() {
        if (selectedBranch == null) {
            return false;
        }

        if (selectedBranch.getType() == BranchType.TOOLBOX) {
            return true;
        }

        return false;
    }

    public boolean isToolboxOrDraftBranch() {
        if (selectedBranch == null) {
            return false;
        }

        if (selectedBranch.getType() == BranchType.TOOLBOX || selectedBranch.getType() == BranchType.DRAFT) {
            return true;
        }

        return false;
    }

    public void verificationChanged() throws NotFoundException {
        selectedCustomVerification.getCustomVerificationParams().clear();
        initCustomVerificationParams();
    }

    public void branchChanged() {
        this.job.setBranch(selectedBranch);
        verificationConfRows.clear();
        initVerificationConfRows();
        clearExistingReports();
        initReports();
        job.setTriggerType(JobTriggerType.MANUAL);
        job.setTriggerScope(JobTriggerScope.USER);
    }

    public List<VerificationConfRow> getVerificationConfRows() {
        return verificationConfRows;
    }

    public List<SelectItem> fetchCustomParamValueItems(CustomVerificationParam param) {
        return customParamValueItemsMap.get(param);
    }

    public void addCustomVerification() throws NotFoundException {
        selectedCustomVerification = new JobCustomVerification();
        selectedCustomVerification.setVerification(verifications.get(0));
        selectedCustomVerification.setCustomVerificationConfs(new ArrayList<CustomVerificationConf>());
        selectedCustomVerification.setCustomVerificationParams(new ArrayList<CustomVerificationParam>());
        initCustomVerificationParams();
    }

    public void createCustomParameter() {
        customParameter = new JobCustomParameter();
    }

    public void storeCustomParameter() {
        if (customParameters.contains(customParameter)) {
            return;
        }

        customParameters.add(customParameter);
    }

    public void deleteCustomParameter(JobCustomParameter customParameter) {
        customParameters.remove(customParameter);
    }

    public void saveCustomVerification() throws NotFoundException {
        log.info("Saving custom verification {} for job {}", selectedCustomVerification, job);

        if (!job.getCustomVerifications().contains(selectedCustomVerification)) {
            RelationUtil.relate(job, selectedCustomVerification);
            List<ProjectVerificationConf> projectConfs = projectEJB.getVerificationConfs(projectId);
            List<BranchVerificationConf> branchConfs = null;
            if (selectedBranch != null && selectedBranch.getId() != null) {
                branchConfs = branchEJB.getVerificationConfs(selectedBranch.getId());
            }
            List<CustomVerificationConf> customConfs = new ArrayList<CustomVerificationConf>();
            if (selectedCustomVerification.getId() != null) {
                customConfs = jobCustomVerificationEJB.getCustomVerificationConfs(selectedCustomVerification.getId());
            }
            VerificationConfRow row = UIVerificationConfUtil.populateCustomVerificationRow(products, selectedCustomVerification, projectConfs, branchConfs, customConfs);
            verificationConfRows.add(row);
        }

        if (job.getId() == null) {
            return;
        }

        removeDefaultParameters(selectedCustomVerification.getCustomVerificationParams());
        if (selectedCustomVerification.getId() == null) {
            jobCustomVerificationEJB.create(selectedCustomVerification);
            return;
        }
        jobCustomVerificationEJB.update(selectedCustomVerification);
    }

    public void selectCustomVerification(VerificationConfRow row) throws NotFoundException {
        selectedCustomVerification = (JobCustomVerification) row.getCustomVerification();
        if (selectedCustomVerification.getId() != null) {
            initCustomVerificationParams();
            return;
        }
        initCustomParameterValueItemsMap();
    }

    public VerificationConfRow getSelectedVerificationConfRow() {
        return selectedVerificationConfRow;
    }

    public void setSelectedVerificationConfRow(VerificationConfRow verificationConfRow) {
        this.selectedVerificationConfRow = verificationConfRow;
    }

    public void selectVerificationConfRow(VerificationConfRow row) {
        setSelectedVerificationConfRow(row);
    }

    public void deleteCustomVerification(VerificationConfRow row) throws NotFoundException {
        log.info("Deleting custom verification row{} for job {}", row, job);
        verificationConfRows.remove(row);
        JobCustomVerification customVerification = (JobCustomVerification) row.getCustomVerification();
        job.getCustomVerifications().remove(customVerification);
        if (customVerification.getId() != null) {
            jobCustomVerificationEJB.delete(customVerification);
        }
    }

    public void completeFileUpload(UserFile userFile) {

        try {
            if (selectedJobVerificationConf != null) {
                jobVerificationConfEJB.addFile(selectedJobVerificationConf.getId(), userFile);
            }

            if (selectedCustomVerificationConf != null) {
                customVerificationConfEJB.addFile(selectedCustomVerificationConf.getId(), userFile);
            }
        } catch (Exception e) {
            log.error("Exception when completing file upload. Detailed reasons: {}.", e.getMessage() + e.getStackTrace());
        }
    }

    public boolean updateFileRelation(List<UserFile> sourceFileList, List<UserFile> targetFileList) {
        try {
            List<UserFile> allFiles = new ArrayList<UserFile>();
            allFiles.addAll(sourceFileList);
            allFiles.addAll(targetFileList);

            if (selectedJobVerificationConf != null) {
                jobVerificationConfEJB.updateFiles(selectedJobVerificationConf.getId(),
                        allFiles, targetFileList);
            }

            if (selectedCustomVerificationConf != null) {
                customVerificationConfEJB.updateFiles(selectedCustomVerificationConf.getId(),
                        allFiles, targetFileList);
            }
            return true;
        } catch (Exception e) {
            log.error("Exception when update file relation. Detailed reasons: {}.", e.getMessage() + e.getStackTrace());
            return false;
        }
    }

    public List<UserFile> filterAvailableFiles(List<UserFile> inUserFiles) {

        List<UserFile> outUserFiles = new ArrayList<UserFile>();

        //Filter from the input files which are accessible for current user, filter out the files not related to confs.
        try {
            if (selectedJobVerificationConf != null) {
                List<UserFile> alreadyRelatedUserFiles = jobVerificationConfEJB.getFiles(selectedJobVerificationConf.getId());

                for (UserFile curUserFile : inUserFiles) {
                    if (!alreadyRelatedUserFiles.contains(curUserFile)) {
                        outUserFiles.add(curUserFile);
                    }
                }
            }

            if (selectedCustomVerificationConf != null) {
                List<UserFile> alreadyRelatedUserFiles = customVerificationConfEJB.getFiles(selectedCustomVerificationConf.getId());

                for (UserFile curUserFile : inUserFiles) {
                    if (!alreadyRelatedUserFiles.contains(curUserFile)) {
                        outUserFiles.add(curUserFile);
                    }
                }
            }

            if (selectedJobVerificationConf == null && selectedCustomVerificationConf == null) {
                outUserFiles = inUserFiles;
            }
        } catch (Exception e) {
            log.error("Exception when filtering non-related files. Detailed reasons: {}.", e.getMessage() + e.getStackTrace());
        }

        return outUserFiles;

    }

    public List<UserFile> filterRelatedFiles(List<UserFile> inUserFiles) {

        List<UserFile> outUserFiles = new ArrayList<UserFile>();

        //Filter from the input files which are accessible for current user, filter out the files which are related to confs.
        try {
            if (selectedJobVerificationConf != null) {
                List<UserFile> alreadyRelatedUserFiles = jobVerificationConfEJB.getFiles(selectedJobVerificationConf.getId());

                for (UserFile curUserFile : inUserFiles) {
                    if (alreadyRelatedUserFiles.contains(curUserFile)) {
                        outUserFiles.add(curUserFile);
                    }
                }
            }

            if (selectedCustomVerificationConf != null) {
                List<UserFile> alreadyRelatedUserFiles = customVerificationConfEJB.getFiles(selectedCustomVerificationConf.getId());

                for (UserFile curUserFile : inUserFiles) {
                    if (alreadyRelatedUserFiles.contains(curUserFile)) {
                        outUserFiles.add(curUserFile);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception when filtering related files. Detailed reasons: {}.", e.getMessage() + e.getStackTrace());
        }

        return outUserFiles;

    }

    public String save() throws NotFoundException {
        log.debug("Save triggered!");

        if (!httpSessionBean.isLoggedIn()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Job save failed.", "You have to login first!");
            return "login?faces-redirect=true";
        }

        if (!hasConfigurationSelected() && !httpSessionBean.hasAdminAccessToProject(projectId)) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Job save failed.", "There must be one or more configuration selected.");
            return null;
        }
        if (selectedBranch == null) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Job save failed.", "Job does not have any branch selected.");
            return null;
        }

        if (job == null || StringUtils.isEmpty(job.getDisplayName())) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Job save failed.", "Please fill verification name!");
            return null;
        }
        if (job.getTriggerType() == JobTriggerType.AUTOMATIC && job.getTriggerScope() == null) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Job save failed.", "Please select trigger scope for the job.");
            return null;
        }

        if (job.getId() != null) {
            log.debug("Updating existing job {}", job);
            if (!httpSessionBean.hasAdminAccessToProject(projectId)) {
                if (job.getOwner() == null || !httpSessionBean.getSysUserId().equals(job.getOwner().getId())) {
                    addMessage(FacesMessage.SEVERITY_INFO,
                            "Unauthorized operation.", "This is not your job!");
                    return null;
                }
            }
            job.setBranch(selectedBranch);
            job.setProjectId(selectedBranch.getProject().getId());
            handleReportActions();
            // TODO: change to update() when git information is separated into
            // another table
            job = jobEJB.updateFromUI(job);
        } else {
            log.info("Saving new job for project {} by user {}", projectId,
                    httpSessionBean.getSysUserId());

            // Remove default paramters (param value matches
            // VerificationConfRow.DEFAULT_PARAM_VALUE)
            for (JobCustomVerification jcv : job.getCustomVerifications()) {
                removeDefaultParameters(jcv.getCustomVerificationParams());
            }

            try {
                handleReportActions();
                jobEJB.create(job, selectedBranch.getId(), httpSessionBean.getSysUserId());
            } catch (NotFoundException ex) {
                addMessage(FacesMessage.SEVERITY_INFO,
                        "Configuration error.", "Required entries not found.");
                return null;
            }
        }

        boolean saveSuccess = saveVerificationConfs();
        saveSuccess = saveSuccess && saveStatusTriggerPattern();
        saveSuccess = saveSuccess && saveFileTriggerPattern();
        saveSuccess = saveSuccess && saveCustomParameters();

        if (saveSuccess) {
            return "verificationDetails?faces-redirect=true&verificationId="
                    + job.getId();
        }

        return null;
    }

    public String cancelEdit() {
        String action = null;

        if (job.getId() != null) {
            action = "verificationDetails?faces-redirect=true&verificationId="
                    + job.getId();
        } else if (projectId != null) {
            action = "projectDetails?faces-redirect=true&projectId="
                    + projectId.toString();
        }
        return action;
    }

    public Long getProjectId() {
        return this.projectId;
    }

    public List<Branch> getAvailableBranches() {
        return availableBranches;
    }

    public void setAvailableBranches(List<Branch> availableBranches) {
        this.availableBranches = availableBranches;
    }

    public Branch getSelectedBranch() {
        return selectedBranch;
    }

    public void setSelectedBranch(Branch selectedBranch) {
        this.selectedBranch = selectedBranch;
    }

    public boolean isEnabled() {
        if (job == null || job.getDisabled() == null) {
            return true;
        }
        return !job.getDisabled();
    }

    public void setEnabled(boolean enabled) {
        if (job != null) {
            job.setDisabled(Boolean.valueOf(!enabled));
        }
    }

    private void initCustomParameters() {
        customParameters = jobEJB.getCustomParameters(job.getId());
    }

    private void initVerificationConfRows() {
        List<ProjectVerificationConf> projectConfs = projectEJB.getVerificationConfs(projectId);
        List<BranchVerificationConf> branchConfs = null;
        List<TemplateVerificationConf> templateConfs = new ArrayList<TemplateVerificationConf>();
        Template template = null;
        if (selectedBranch != null) {
            branchConfs = branchEJB.getVerificationConfs(selectedBranch.getId());
            template = selectedBranch.getTemplate();
            if (template != null) {
                templateConfs = templateEJB.getVerificationConfs(template.getId());
            }
        }
        List<JobVerificationConf> jobConfs = new ArrayList<JobVerificationConf>();

        if (job.getId() != null) {
            jobConfs = jobEJB.getVerificationConfs(job.getId());
        }

        verificationConfRows = UIVerificationConfUtil.populateJobVerificationConfRows(products, verifications, projectConfs, branchConfs, jobConfs, templateConfs);

        List<VerificationConfRow> customVerificationConfRows = getCustomVerificationConfRows(projectConfs, branchConfs, template);
        verificationConfRows.addAll(customVerificationConfRows);
    }

    private List<VerificationConfRow> getCustomVerificationConfRows(List<ProjectVerificationConf> projectConfs, List<BranchVerificationConf> branchConfs, Template template) {
        List<VerificationConfRow> customConfRows = new ArrayList<VerificationConfRow>();
        if (job.getId() == null) {
            return customConfRows;
        }
        List<JobCustomVerification> customVerifications = jobEJB.getCustomVerifications(job.getId());
        for (JobCustomVerification customVerification : customVerifications) {
            List<CustomVerificationConf> customVerificationConfs = jobCustomVerificationEJB.getCustomVerificationConfs(customVerification.getId());
            List<CustomVerificationParam> customVerificationParams = jobCustomVerificationEJB.getCustomVerificationParams(customVerification.getId());
            customVerification.setCustomVerificationParams(customVerificationParams);
            VerificationConfRow row = UIVerificationConfUtil.populateCustomVerificationRow(products, customVerification, projectConfs, branchConfs, customVerificationConfs);
            customConfRows.add(row);
        }

        if (template == null) {
            return customConfRows;
        }

        List<TemplateCustomVerification> templateCustomVerifications = templateEJB.getCustomVerifications(template.getId());
        for (TemplateCustomVerification customVerification : templateCustomVerifications) {
            List<TemplateCustomVerificationConf> customVerificationConfs = templateCustomVerificationEJB.getCustomVerificationConfs(customVerification.getId());
            List<TemplateCustomVerificationParam> customVerificationParams = templateCustomVerificationEJB.getCustomVerificationParams(customVerification.getId());
            customVerification.setCustomVerificationParams(customVerificationParams);
            VerificationConfRow row = UIVerificationConfUtil.populateTemplateCustomVerificationRow(products, customVerification, projectConfs, branchConfs, customVerificationConfs);
            customConfRows.add(row);
        }

        return customConfRows;
    }

    private void initCustomParameterValueItemsMap() {
        customParamValueItemsMap = new HashMap<CustomVerificationParam, List<SelectItem>>();
        for (CustomVerificationParam cvp : selectedCustomVerification.getCustomVerificationParams()) {
            List<SelectItem> items = new ArrayList<SelectItem>();
            SelectItem defaultItem = new SelectItem(VerificationConfRow.DEFAULT_PARAM_VALUE, VerificationConfRow.DEFAULT_PARAM_VALUE);
            items.add(defaultItem);

            boolean matchFound = false;
            for (CustomParamValue value : cvp.getCustomParam().getCustomParamValues()) {
                if (cvp.getParamValue() == null || cvp.getParamValue().equals(value.getParamValue())) {
                    matchFound = true;
                }
                SelectItem item = new SelectItem(value.getParamValue(), value.getParamValue());
                if (StringUtils.isEmpty(value.getParamValue())) {
                    // Workaround for PrimeFaces bug https://code.google.com/p/primefaces/issues/detail?id=5624
                    item.setLabel(VerificationConfRow.EMPTY_PARAM_VALUE);
                    item.setValue(VerificationConfRow.EMPTY_PARAM_VALUE);
                }
                items.add(item);
            }

            if (!matchFound && cvp.getParamValue() != null) {
                SelectItem matched = new SelectItem(cvp.getParamValue(), cvp.getParamValue());
                if ("".equals(cvp.getParamValue())) {
                    matched.setLabel(VerificationConfRow.EMPTY_PARAM_VALUE);
                    matched.setValue(VerificationConfRow.EMPTY_PARAM_VALUE);
                }
                items.add(1, matched);
            }

            if (cvp.getParamValue() == null) {
                cvp.setParamValue(VerificationConfRow.DEFAULT_PARAM_VALUE);
            } else if ("".equals(cvp.getParamValue())) {
                cvp.setParamValue(VerificationConfRow.EMPTY_PARAM_VALUE);
            }

            customParamValueItemsMap.put(cvp, items);
        }
    }

    private boolean saveVerificationConfs() throws NotFoundException {
        log.debug("Saving verification confs for job {}", job);
        if (job == null || job.getId() == null) {
            return false;
        }

        List<JobVerificationConf> confs = new ArrayList<JobVerificationConf>();

        for (VerificationConfRow row : verificationConfRows) {
            List<CustomVerificationConf> customConfs = new ArrayList<CustomVerificationConf>();
            for (VerificationConfCell cell : row.getCells()) {
                if (!cell.isSelected() || !cell.isEnabled() || cell.isTemplate()) {
                    continue;
                }
                if (row.isCustom()) {
                    addCustomVerificationConf(row, cell, customConfs);
                    continue;
                }
                addJobVerificationConf(cell, confs);
            }
            if (row.isCustom() && !row.isTemplate()) {
                jobCustomVerificationEJB.saveCustomVerificationConfs(row.getCustomVerification().getId(), customConfs);
            }
        }
        if (!isToolboxOrDraftBranch()) {
            log.debug("Saving pre and post verification confs for job {}", job);
            jobEJB.savePreVerifications(job.getId(), preBuildVerifications.getTarget());
            jobEJB.savePostVerifications(job.getId(), postBuildVerifications.getTarget());
        } else {
            // TODO: Remove save skipping when available pre and post
            // verifications are set on branch level.
            log.debug("Clearing pre and post verification confs for job {} because job type is toolbox", job);
            jobEJB.savePreVerifications(job.getId(), new ArrayList<Verification>());
            jobEJB.savePostVerifications(job.getId(), new ArrayList<Verification>());
        }
        jobEJB.saveVerificationConfs(job.getId(), confs);
        return true;
    }

    private void addJobVerificationConf(VerificationConfCell cell, List<JobVerificationConf> confs) {
        JobVerificationConf conf = new JobVerificationConf();
        Product p = new Product();
        p.setId(cell.getProductId());
        Verification v = new Verification();
        v.setId(cell.getVerificationId());
        conf.setProduct(p);
        conf.setVerification(v);
        conf.setCardinality(cell.getCardinality());
        if (cell.getDevice() != null) {
            conf.setImeiCode(cell.getDevice().getImei());
            if (!StringUtils.isEmpty(cell.getDevice().getTasHostname())
                    && !StringUtils.isEmpty(cell.getDevice().getPort())) {
                conf.setTasUrl(cell.getDevice().getTasHostname() + ":" + cell.getDevice().getTasPort());
            }
        }
        confs.add(conf);
    }

    private void addCustomVerificationConf(VerificationConfRow row, VerificationConfCell cell, List<CustomVerificationConf> customConfs) {
        CustomVerificationConf customConf = new CustomVerificationConf();
        Product p = new Product();
        p.setId(cell.getProductId());
        customConf.setProduct(p);
        customConf.setCardinality(cell.getCardinality());

        if (cell.getDevice() != null) {
            customConf.setImeiCode(cell.getDevice().getImei());
            if (!StringUtils.isEmpty(cell.getDevice().getTasHostname())
                    && !StringUtils.isEmpty(cell.getDevice().getPort())) {
                customConf.setTasUrl(cell.getDevice().getTasHostname() + ":" + cell.getDevice().getTasPort());
            }
        }

        JobCustomVerification v = new JobCustomVerification();
        v.setId(row.getCustomVerification().getId());
        customConf.setJobCustomVerification(v);

        customConfs.add(customConf);
    }

    private boolean hasConfigurationSelected() {
        if (verificationConfRows == null) {
            return false;
        }
        for (VerificationConfRow row : verificationConfRows) {
            for (VerificationConfCell cell : row.getCells()) {
                if (cell.isSelected()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void initProducts() {
        products = projectEJB.getProducts(projectId);
    }

    private void initPrePostVerifications() throws NotFoundException {
        postBuildVerifications = new DualListModel<Verification>();
        preBuildVerifications = new DualListModel<Verification>();
        List<Verification> jobPreVerifications = new ArrayList<Verification>();
        List<Verification> jobPostVerifications = new ArrayList<Verification>();

        List<Verification> availablePreVerifications = projectEJB.getPreVerifications(projectId);
        List<Verification> availablePostVerifications = projectEJB.getPostVerifications(projectId);

        if (job.getId() != null) {
            jobPreVerifications = jobEJB.getPreVerifications(job.getId());
            jobPostVerifications = jobEJB.getPostVerifications(job.getId());
        }

        availablePreVerifications.removeAll(jobPreVerifications);
        availablePostVerifications.removeAll(jobPostVerifications);

        preBuildVerifications.setSource(availablePreVerifications);
        preBuildVerifications.setTarget(jobPreVerifications);

        postBuildVerifications.setSource(availablePostVerifications);
        postBuildVerifications.setTarget(jobPostVerifications);
    }

    private void initVerifications() {
        verifications = projectEJB.getVerifications(projectId);
    }

    private void initAvailableBranches() {
        if (httpSessionBean.hasAdminAccessToProject(projectId)) {
            availableBranches = projectEJB.getBranches(projectId);
        } else {
            availableBranches = new ArrayList<Branch>();
            for (Branch b : projectEJB.getBranches(projectId)) {
                if (b.getType() == BranchType.TOOLBOX || b.getType() == BranchType.DRAFT) {
                    availableBranches.add(b);
                }
            }
            if (availableBranches.isEmpty()) {
                addMessage(FacesMessage.SEVERITY_INFO,
                        "Toolbox not enabled for this project.", "This project does not support toolbox or draft verifications yet. Please, try again later.");
            }
        }
    }

    private void initCustomVerificationParams() {
        List<CustomVerificationParam> customVerificationParams = new ArrayList<CustomVerificationParam>();
        selectedCustomVerification.setCustomVerificationParams(customVerificationParams);
        if (selectedCustomVerification.getId() != null) {
            customVerificationParams = jobCustomVerificationEJB.getCustomVerificationParams(selectedCustomVerification.getId());
        }

        List<CustomParam> customParams = verificationEJB.getCustomParams(selectedCustomVerification.getVerification().getId());
        for (CustomParam param : customParams) {
            List<CustomParamValue> customParamValues = customParamEJB.getCustomParamValues(param.getId());
            param.setCustomParamValues(customParamValues);
            CustomVerificationParam customVerificationParam = findCustomVerificationParam(customVerificationParams, param);
            customVerificationParam.setCustomParam(param);
            selectedCustomVerification.getCustomVerificationParams().add(customVerificationParam);
        }
        initCustomParameterValueItemsMap();
    }

    private CustomVerificationParam findCustomVerificationParam(List<CustomVerificationParam> customVerificationParams, CustomParam param) {
        for (CustomVerificationParam cvp : customVerificationParams) {
            if (param.equals(cvp.getCustomParam())) {
                return cvp;
            }
        }
        CustomVerificationParam customVerificationParam = new CustomVerificationParam();
        customVerificationParam.setCustomVerification(selectedCustomVerification);
        return customVerificationParam;
    }

    private void initJobCustomVerifications() {
        if (job.getId() == null) {
            return;
        }
        job.setCustomVerifications(jobEJB.getCustomVerifications(job.getId()));
    }

    private void removeDefaultParameters(List<CustomVerificationParam> customVerificationParams) throws NotFoundException {
        List<CustomVerificationParam> removed = new ArrayList<CustomVerificationParam>();
        for (CustomVerificationParam param : customVerificationParams) {
            if (VerificationConfRow.DEFAULT_PARAM_VALUE.equals(param.getParamValue())) {
                removed.add(param);
                if (param.getId() != null) {
                    customVerificationParamEJB.delete(param);
                }
            }

            if (VerificationConfRow.EMPTY_PARAM_VALUE.equals(param.getParamValue())) {
                param.setParamValue("");
            }
        }
        customVerificationParams.removeAll(removed);
    }

    public void selectAllVerifications() {
        for (VerificationConfRow row : verificationConfRows) {
            for (VerificationConfCell cell : row.getCells()) {
                cell.setSelected(true);
            }
        }
    }

    public void clearAllVerifications() {
        for (VerificationConfRow row : verificationConfRows) {
            for (VerificationConfCell cell : row.getCells()) {
                cell.setSelected(false);
            }
        }
    }

    private void initReports() {
        if (selectedBranch == null) {
            return;
        }

        emailReports.clear();
        gerritReports.clear();
        notificationReports.clear();

        // EnumMap retains Enum natural order automatically.
        Map<ReportActionTitle, ReportModel<EmailReportAction>> emailReportMap = new EnumMap<ReportActionTitle, ReportModel<EmailReportAction>>(ReportActionTitle.class);
        Map<ReportActionTitle, ReportModel<GerritReportAction>> gerritReportMap = new EnumMap<ReportActionTitle, ReportModel<GerritReportAction>>(ReportActionTitle.class);
        Map<ReportActionTitle, ReportModel<NotificationReportAction>> notificationReportMap = new EnumMap<ReportActionTitle, ReportModel<NotificationReportAction>>(ReportActionTitle.class);

        if (selectedBranch.getType() == BranchType.DEVELOPMENT || selectedBranch.getType() == BranchType.MASTER) {
            emailReportMap.put(ReportActionTitle.SUCCESS, new ReportModel<EmailReportAction>(false, ReportActionStatus.SUCCESS, new EmailReportAction(), ReportActionTitle.SUCCESS));
            emailReportMap.put(ReportActionTitle.UNSTABLE, new ReportModel<EmailReportAction>(false, ReportActionStatus.UNSTABLE, new EmailReportAction(), ReportActionTitle.UNSTABLE));
            emailReportMap.put(ReportActionTitle.FAILURE, new ReportModel<EmailReportAction>(false, ReportActionStatus.FAILURE, new EmailReportAction(), ReportActionTitle.FAILURE));
        }

        if (selectedBranch.getType() == BranchType.DEVELOPMENT) {
            notificationReportMap.put(ReportActionTitle.NOTIFY_UNSTABLE_MERGE, new ReportModel<NotificationReportAction>(false, ReportActionStatus.STARTED, new NotificationReportAction(), ReportActionTitle.NOTIFY_UNSTABLE_MERGE));
        }

        gerritReportMap.put(ReportActionTitle.SUCCESS, new ReportModel<GerritReportAction>(false, ReportActionStatus.SUCCESS, new GerritReportAction(), ReportActionTitle.SUCCESS));
        gerritReportMap.put(ReportActionTitle.UNSTABLE, new ReportModel<GerritReportAction>(false, ReportActionStatus.UNSTABLE, new GerritReportAction(), ReportActionTitle.UNSTABLE));
        gerritReportMap.put(ReportActionTitle.FAILURE, new ReportModel<GerritReportAction>(false, ReportActionStatus.FAILURE, new GerritReportAction(), ReportActionTitle.FAILURE));
        if (selectedBranch.getType() == BranchType.SINGLE_COMMIT) {
            gerritReportMap.put(ReportActionTitle.UNSTABLE_NO_BLOCKING, new ReportModel<GerritReportAction>(false, ReportActionStatus.UNSTABLE, new GerritReportAction(), ReportActionTitle.UNSTABLE_NO_BLOCKING));
        }

        //Load existing report actions
        List<ReportAction> reportActions = jobEJB.getReportActions(job.getId());

        for (ReportAction action : reportActions) {

            if (action == null || action.getStatus() == null || action.getTitle() == null) {
                log.warn("Error occurred while loading report action for job id={}", job.getId());
                continue;
            }

            if (action instanceof EmailReportAction) {
                ReportModel<EmailReportAction> reportModel = new ReportModel<EmailReportAction>(true, action.getStatus(), (EmailReportAction) action, action.getTitle());
                emailReportMap.put(action.getTitle(), reportModel);
            } else if (action instanceof GerritReportAction) {
                ReportModel<GerritReportAction> reportModel = new ReportModel<GerritReportAction>(true, action.getStatus(), (GerritReportAction) action, action.getTitle());
                gerritReportMap.put(action.getTitle(), reportModel);
            } else if (action instanceof NotificationReportAction) {
                ReportModel<NotificationReportAction> reportModel = new ReportModel<NotificationReportAction>(true, action.getStatus(), (NotificationReportAction) action, action.getTitle());
                notificationReportMap.put(action.getTitle(), reportModel);
            }
        }

        emailReports.addAll(emailReportMap.values());
        gerritReports.addAll(gerritReportMap.values());
        notificationReports.addAll(notificationReportMap.values());
    }

    private void handleReportActions() {
        List<ReportModel> reports = new ArrayList<ReportModel>();
        reports.addAll(emailReports);
        gerritScoresAsNull();
        gerritAbandonForToolbox();
        reports.addAll(gerritReports);
        reports.addAll(notificationReports);

        List<ReportAction> actions = new ArrayList<ReportAction>();
        for (ReportModel model : reports) {
            if (model.isEnabled()) {
                ReportAction action = model.getAction();
                action.setStatus(model.getStatus());
                action.setJob(job);
                action.setTitle(model.getTitle());
                actions.add(action);
                continue;
            }
            deleteReportAction(model);
        }
        job.setReportActions(actions);
    }

    /**
     * Convert all verify and review scores with value {@link Integer.MIN_VALUE}
     * to null. This is need for because JSF is forcing integers to zero value
     * even you have converter to deal null values. Passing
     * -Dorg.apache.el.parser.COERCE_TO_ZERO=false property to JVM should fix
     * this problem but because it has global effect to web application layer it
     * is not used without good testing. More information about JSF behavior:
     * http://java.net/jira/browse/JSP_SPEC_PUBLIC-184
     */
    private void gerritScoresAsNull() {
        for (ReportModel<GerritReportAction> reportModel : gerritReports) {
            GerritReportAction action = reportModel.getAction();
            if (action.getReviewScore() != null && action.getReviewScore() == Integer.MIN_VALUE) {
                action.setReviewScore(null);
            }
            if (action.getVerifiedScore() != null && action.getVerifiedScore() == Integer.MIN_VALUE) {
                action.setVerifiedScore(null);
            }
        }
    }

    /**
     * Set abandon true for all Gerrit actions if job is toolbox job. Abandon
     * checkbox is removed from UI and all toolbox changes should be abandon in
     * Gerrit. This is because without abandoning, automatic triggered toolbox
     * changes would be verified in endless loop. ( Toolbox doesn't have
     * verified scores in Gerrit).
     */
    private void gerritAbandonForToolbox() {
        if (!isToolboxBranch()) {
            return;
        }
        for (ReportModel<GerritReportAction> reportModel : gerritReports) {
            GerritReportAction action = reportModel.getAction();
            action.setAbandon(true);
        }
    }

    private void clearExistingReports() {
        List<ReportModel> reports = new ArrayList<ReportModel>();
        reports.addAll(emailReports);
        reports.addAll(gerritReports);
        reports.addAll(notificationReports);
        for (ReportModel model : reports) {
            deleteReportAction(model);
        }
    }

    private void deleteReportAction(ReportModel model) {
        if (model.getAction().getId() == null) {
            return;
        }

        try {
            reportActionEJB.delete(model.getAction());
        } catch (NotFoundException ex) {
            log.warn("Report action can not be deleted! Cause: {}", ex.getMessage());
        }
    }

    private void initStatusTriggerPatterns() {
        statusTriggerPatterns = jobEJB.getStatusTriggerPatterns(job.getId());
        resetNewStatusTrigger();
    }

    private void initFileTriggerPatterns() {
        fileTriggerPatterns = jobEJB.getFileTriggerPatterns(job.getId());
        resetNewFileTrigger();
    }

    private void resetNewStatusTrigger() {
        newStatusTrigger = new StatusTriggerPattern();
        newStatusTrigger.setJob(job);
        newStatusTrigger.setPattern("");
    }

    private void resetNewFileTrigger() {
        newFileTrigger = new FileTriggerPattern();
        newFileTrigger.setJob(job);
        newFileTrigger.setFilepath("");
    }

    public List<StatusTriggerPattern> getStatusTriggerPatterns() {
        return statusTriggerPatterns;
    }

    public List<FileTriggerPattern> getFileTriggerPatterns() {
        return fileTriggerPatterns;
    }

    public StatusTriggerPattern getNewStatusTrigger() {
        return newStatusTrigger;
    }

    public FileTriggerPattern getNewFileTrigger() {
        return newFileTrigger;
    }

    public void setNewStatusTrigger(StatusTriggerPattern newStatusTrigger) {
        this.newStatusTrigger = newStatusTrigger;
    }

    public void setNewFileTrigger(FileTriggerPattern newFileTrigger) {
        this.newFileTrigger = newFileTrigger;
    }

    @RolesAllowed("SYSTEM_ADMIN")
    public void addStatusTriggerPattern() {
        FacesMessage message = calcInputValidationMessage(newStatusTrigger);
        if (message == null) {
            statusTriggerPatterns.add(newStatusTrigger);
            resetNewStatusTrigger();
        } else {
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    @RolesAllowed("SYSTEM_ADMIN")
    public void addFileTriggerPattern() {
        FacesMessage message = calcInputValidationMessage(newFileTrigger);
        if (message == null) {
            fileTriggerPatterns.add(newFileTrigger);
            resetNewFileTrigger();
        } else {
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }
    }

    private FacesMessage calcInputValidationMessage(StatusTriggerPattern newStatusTrigger) {
        if (newStatusTrigger.getPattern().trim().length() <= 0) {
            return new FacesMessage(FacesMessage.SEVERITY_INFO, "Status trigger pattern cannot be empty", "");
        }
        if (!hasOnlyCharactersSorF(newStatusTrigger)) {
            return new FacesMessage(FacesMessage.SEVERITY_INFO, "Invalid input. You could specified pattern using these characters: S, F", "");
        }
        if (!isStatusTriggerPatternUnique(newStatusTrigger)) {
            return new FacesMessage(FacesMessage.SEVERITY_INFO, "Status trigger pattern already exists", "");
        }
        return null;
    }

    private FacesMessage calcInputValidationMessage(FileTriggerPattern newFileTrigger) {
        if (StringUtils.isEmpty(newFileTrigger.getFilepath())) {
            return new FacesMessage(FacesMessage.SEVERITY_INFO, "File trigger file path cannot be empty", "");
        }
        if (newFileTrigger.getFilepath().trim().length() <= 0) {
            return new FacesMessage(FacesMessage.SEVERITY_INFO, "File trigger file path cannot be empty", "");
        }
        if (!isFileTriggerPatternUnique(newFileTrigger)) {
            return new FacesMessage(FacesMessage.SEVERITY_INFO, "File trigger file path already exists", "");
        }
        return null;
    }

    private boolean hasOnlyCharactersSorF(StatusTriggerPattern newStatusTrigger) {
        Pattern pattern = Pattern.compile("(S|F)*");
        Matcher matcher = pattern.matcher(newStatusTrigger.getPattern());
        return matcher.matches();
    }

    private boolean isStatusTriggerPatternUnique(StatusTriggerPattern newStatusTrigger) {
        for (StatusTriggerPattern trigger : statusTriggerPatterns) {
            if (trigger.getPattern().equals(newStatusTrigger.getPattern())) {
                return false;
            }
        }
        return true;
    }

    private boolean isFileTriggerPatternUnique(FileTriggerPattern newFileTrigger) {
        for (FileTriggerPattern trigger : fileTriggerPatterns) {
            if (trigger.getFilepath().equals(newFileTrigger.getFilepath())) {
                return false;
            }
        }
        return true;
    }

    public void deleteStatusTriggerPattern(StatusTriggerPattern pattern) {
        statusTriggerPatterns.remove(pattern);
    }

    public void deleteFileTriggerPattern(FileTriggerPattern pattern) {
        fileTriggerPatterns.remove(pattern);
    }

    private boolean saveStatusTriggerPattern() throws NotFoundException {
        log.debug("Saving statusTriggerPatterns for job {}", job);
        if (job == null || job.getId() == null) {
            return false;
        }
        jobEJB.saveStatusTriggerPatterns(job.getId(), statusTriggerPatterns);
        return true;
    }

    private boolean saveFileTriggerPattern() throws NotFoundException {
        log.debug("Saving fileTriggerPatterns for job {}", job);
        if (job == null || job.getId() == null) {
            return false;
        }
        jobEJB.saveFileTriggerPatterns(job.getId(), fileTriggerPatterns);
        return true;
    }

    public void validateCronExpression(FacesContext context, UIComponent toValidate, Object value) {
        String cronExpression = (String) value;
        try {
            CronExpression.validateExpression(cronExpression);
        } catch (ParseException e) {
            FacesMessage message = new FacesMessage();
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            message.setSummary("Cron expression is not valid");
            message.setDetail("Cron expression format was incorrect.");
            context.addMessage(null, message);
            throw new ValidatorException(message);
        }
    }

    private boolean saveCustomParameters() throws NotFoundException {
        log.debug("Saving global parameters for job {}", job);
        if (job == null || job.getId() == null) {
            return false;
        }
        jobEJB.saveCustomParameters(job.getId(), customParameters);
        return true;
    }

    public JobCustomParameter getCustomParameter() {
        return customParameter;
    }

    public void setCustomParameter(JobCustomParameter customParameter) {
        this.customParameter = customParameter;
    }

    public List<JobCustomParameter> getCustomParameters() {
        return customParameters;
    }

    public void setCustomParameters(List<JobCustomParameter> customParameters) {
        this.customParameters = customParameters;
    }

    public List<String> getTimezones() {
        return TimezoneUtil.getTimezoneList();
    }

    public void selectConfForOptions(VerificationConfRow row, VerificationConfCell cell) {
        if (cell != null && !cell.equals(this.selectedCellForOptions)) {
            if (cell.getDevice() != null) {
                this.selectedTasDevice = cell.getDevice().getImei();
            } else {
                this.selectedTasDevice = null;
            }

            try {
                if (row.isCustom()) {
                    //Find verification conf from CUSTOM_VERIFICATION_CONF
                    selectedCustomVerificationConf = jobEJB.findCustomVerificationConf(job.getId(), row.getCustomVerification().getId(), cell.getProductId());
                    selectedJobVerificationConf = null;
                } else {
                    //Find verification conf from JOB_VERIFICATION_CONF
                    selectedJobVerificationConf = jobEJB.findVerificationConf(job.getId(), cell.getVerificationId(), cell.getProductId());
                    selectedCustomVerificationConf = null;
                }
            } catch (Exception e) {
                log.error("Exception when select cell. Detailed Reason: {}", e.getMessage() + e.getStackTrace());
            }
        }
        this.optionsFilter = "";
        this.selectedCellForOptions = cell;

        updateMatchingTasDevices();
    }

    public String getOptionsFilter() {
        return optionsFilter;
    }

    public void setOptionsFilter(String optionsFilter) {
        this.optionsFilter = optionsFilter;
    }

    public String getSelectedTasDevice() {
        return selectedTasDevice;
    }

    public void setSelectedTasDevice(String selectedTasDevice) {
        this.selectedTasDevice = selectedTasDevice;
    }

    public void storeDeviceInfo() {
        if (this.selectedCellForOptions != null) {
            boolean found = false;
            for (TasDevice device : this.matchingTasDevices) {
                if (device.getImei().equals(this.selectedTasDevice)) {
                    this.selectedCellForOptions.setDevice(device);
                    found = true;
                    break;
                }
            }
            if (found) {
                addMessage(FacesMessage.SEVERITY_INFO,
                        "Selection successful!", "Successfully added TAS device IMEI:" + selectedTasDevice + " as selected device for product " + selectedCellForOptions.getRmCode());
                log.info("Successfully added TAS device {} as selected device", selectedTasDevice);
            } else {
                addMessage(FacesMessage.SEVERITY_INFO,
                        "No TAS device selected", "No TAS device was selected for product " + selectedCellForOptions.getRmCode() + ", using default device.");
                this.selectedCellForOptions.setDevice(null);
            }

            handleFileRelate();
        } else {
            log.warn("Could not add TAS device info, selectedCellForOptions was null!");
        }
    }

    public List<TasDevice> getMatchingTasDevices() {
        if (matchingTasDevices == null) {
            matchingTasDevices = new ArrayList<TasDevice>();
        }
        return matchingTasDevices;
    }

    public void updateMatchingTasDevices() {
        if (allTasDevices == null) {
            try {
                initAllTasDevices();
            } catch (TasProductReadException e) {
                addMessage("optionsDialog", FacesMessage.SEVERITY_ERROR,
                        "TAS device info fetching failed!", "Failed to fetch TAS device info from TAS server. Please, try again or contact support.");
                log.error("Failed to read tas device info! {}", e);
            }
        }

        if (optionsFilter == null) {
            optionsFilter = "";
        }
        matchingTasDevices = TasDeviceFilter.filter(allTasDevices, selectedCellForOptions.getRmCode(), Arrays.asList(optionsFilter.split(" ")));
    }

    public VerificationConfCell getSelectedCellForOptions() {
        return selectedCellForOptions;
    }

    private void initAllTasDevices() throws TasProductReadException {
        allTasDevices = tasReaderEJB.getAllTasDevices();
    }

    public CustomVerificationConf getSelectedCustomVerificationConf() {
        return selectedCustomVerificationConf;
    }

    public JobVerificationConf getSelectedJobVerificationConf() {
        return selectedJobVerificationConf;
    }

    public void updateUserFileRelation() {

        List<UserFile> availableUserFiles = getAvailableUserFiles();
        List<UserFile> relatedUserFiles = getRelatedUserFiles();

        userFileRelationDualList = new DualListModel<UserFile>(availableUserFiles, relatedUserFiles);
    }

    public String getSelectedAccessScope() {
        return selectedAccessScope;
    }

    public void setSelectedAccessScope(String selectedAccessScope) {
        this.selectedAccessScope = selectedAccessScope;
    }

    public String getSelectedOwnershipScope() {
        return selectedOwnershipScope;
    }

    public void setSelectedOwnershipScope(String selectedOwnershipScope) {
        this.selectedOwnershipScope = selectedOwnershipScope;
    }

    public void handleFileRelate() {

        try {
            if (getSelectedJobVerificationConf() == null && getSelectedCustomVerificationConf() == null) {
                addMessage("optionsDialog", FacesMessage.SEVERITY_ERROR,
                        "Conf not saved", "Trying to relate file with a non-existent verification configure, if you just checked a new verification configure, please save it firstly.");
                return;
            }

            boolean updateSucceeded = updateFileRelation(userFileRelationDualList.getSource(), userFileRelationDualList.getTarget());

            if (updateSucceeded) {
                addMessage("optionsDialog", FacesMessage.SEVERITY_INFO, "Success!", "Selected files are related.");
            } else {
                addMessage("optionsDialog", FacesMessage.SEVERITY_ERROR, "Failure!", "Failed to update file relations.");
            }
        } catch (Exception e) {
            log.error("Exception when updating file relations, Detailed reasons: {}.", e.getMessage() + e.getStackTrace());
            addMessage("optionsDialog", FacesMessage.SEVERITY_ERROR, "Failure!", "Exception when updating file relations.");
        }
    }

    public void handleFileUpload(FileUploadEvent event) {

        if (!sysConfigEJB.configExists(SysConfigKey.USER_FILE_UPLOAD_PATH)) {
            log.warn("USER_FILE_UPLOAD_PATH not configured in system setting.");
            addMessage("optionsDialog", FacesMessage.SEVERITY_ERROR,
                    "Missing system configure", "File is not uploaded! USER_FILE_UPLOAD_PATH need to be configured.");
            return;
        }

        UploadedFile uploadedFile = event.getFile();

        if (uploadedFile == null) {
            addMessage("optionsDialog", FacesMessage.SEVERITY_ERROR,
                    "Uploaded file error!", "There was an error in the file you tried to upload!");
            return;
        }

        try {
            String fileUuid = UUID.randomUUID().toString();

            boolean fileCopySucceeded = userFileEJB.copyFile(fileUuid, uploadedFile.getInputstream());

            if (fileCopySucceeded) {
                UserFile userFile = new UserFile();
                userFile.setMimeType(uploadedFile.getContentType());
                userFile.setFileSize(uploadedFile.getSize());
                userFile.setName(uploadedFile.getFileName());
                userFile.setFilePath(userFileEJB.getUploadPath());
                userFile.setUuid(fileUuid);
                userFile.setFileType(FileType.TEST_FILE);
                if (StringUtils.isEmpty(selectedOwnershipScope)) {
                    userFile.setOwnershipScope(OwnershipScope.GLOBAL);
                } else {
                    userFile.setOwnershipScope(OwnershipScope.valueOf(selectedOwnershipScope));
                }
                userFile.setAccessScope(AccessScope.REST);
                sysUserEJB.addFile(httpSessionBean.getSysUser().getId(), userFile);
                if (getSelectedJobVerificationConf() == null && getSelectedCustomVerificationConf() == null) {
                    addMessage("optionsDialog",
                            FacesMessage.SEVERITY_WARN, "Conf not saved yet", "File is uploaded, but not related to current verification conf, current verification conf is not saved yet, please save it firstly and then relate with file.");
                } else {
                    completeFileUpload(userFile);
                    handleFileRelate();
                }
            }
        } catch (Exception e) {
            log.error("Exception when handling upload file {}, detailed reasons: {}.", uploadedFile.getFileName(), e.getMessage() + e.getStackTrace());
            addMessage("optionsDialog",
                    FacesMessage.SEVERITY_ERROR, "Failure!", uploadedFile.getFileName() + " failed to be uploaded.");
        }
    }

    public void changeOwnershipScope(ValueChangeEvent event) {
        selectedOwnershipScope = event.getNewValue().toString();
    }

    public List<UserFile> getAvailableUserFiles() {
        List<UserFile> availableUserFiles = userFileEJB.getAvailableFilesByUserAndType(httpSessionBean.getSysUser().getId(), FileType.TEST_FILE);
        availableUserFiles = filterAvailableFiles(availableUserFiles);
        return availableUserFiles;
    }

    public List<UserFile> getRelatedUserFiles() {
        List<UserFile> relatedUserFiles = userFileEJB.getAvailableFilesByUserAndType(httpSessionBean.getSysUser().getId(), FileType.TEST_FILE);
        relatedUserFiles = filterRelatedFiles(relatedUserFiles);
        return relatedUserFiles;
    }

    public AccessScope[] getAccessScopeValues() {
        return AccessScope.values();
    }

    public OwnershipScope[] getOwnershipScopeValues() {
        return OwnershipScope.values();
    }

    public DualListModel<UserFile> getUserFileRelationDualList() {
        updateUserFileRelation();
        return userFileRelationDualList;
    }

    public void setUserFileRelationDualList(DualListModel<UserFile> userFileRelationDualList) {
        this.userFileRelationDualList = userFileRelationDualList;
    }

    public Project getProject() {
        return project;
    }
}
