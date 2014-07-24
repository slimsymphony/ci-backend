package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.BranchEJB;
import com.nokia.ci.ejb.CIServerEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.TemplateEJB;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.BranchType;
import com.nokia.ci.ejb.model.BranchVerificationConf;
import com.nokia.ci.ejb.model.CIServer;
import com.nokia.ci.ejb.model.GitRepositoryStatus;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.Product;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.ProjectVerificationConf;
import com.nokia.ci.ejb.model.RepositoryType;
import com.nokia.ci.ejb.model.Template;
import com.nokia.ci.ejb.model.Verification;
import com.nokia.ci.ui.model.VerificationConfCell;
import com.nokia.ci.ui.model.VerificationConfRow;
import com.nokia.ci.ui.util.UIVerificationConfUtil;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.DualListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managed bean class for branch.
 *
 * @author jajuutin
 */
@Named
@ViewScoped
public class BranchBean extends AbstractUIBaseBean {

    private static final String NOT_CLONED_MESSAGE = "Not cloned";
    private static final String CLONE_OK_MESSAGE = "Clone OK";
    private static final String CLONE_ONGOING_MESSAGE = "Cloning";
    private static Logger log = LoggerFactory.getLogger(VerificationBean.class);
    private String oldGitRepositoryPath = "";
    private Branch branch;
    private DualListModel<Job> jobs = new DualListModel<Job>();
    private DualListModel<CIServer> servers = new DualListModel<CIServer>();
    private List<Product> products = new ArrayList<Product>();
    private List<Verification> verifications = new ArrayList<Verification>();
    private List<VerificationConfRow> verificationConfRows = new ArrayList<VerificationConfRow>();
    private String projectId;
    private List<Template> templates = new ArrayList<Template>();
    private Project selectedProject;
    private List<Project> projects = new ArrayList<Project>();
    private String branchId;
    @Inject
    private BranchEJB branchEJB;
    @Inject
    private JobEJB jobEJB;
    @Inject
    private CIServerEJB serverEJB;
    @Inject
    private ProjectEJB projectEJB;
    @Inject
    private TemplateEJB templateEJB;
    @Inject
    private SysUserEJB sysUserEJB;
    @Inject
    private HttpSessionBean httpSessionBean;

    @Override
    protected void init() throws BackendAppException {
        branchId = getQueryParam("branchId");
        projectId = getQueryParam("projectId");

        if (branchId == null) {
            branch = new Branch();
            branch.setType(BranchType.DEVELOPMENT);
            branch.setGitRepositoryPath("");
            branch.setGitRepositoryStatus(GitRepositoryStatus.UNINITIALIZED);
        } else {
            log.debug("Finding branch {} for editing!", branch);
            branch = branchEJB.read(Long.parseLong(branchId));
            if (branch.getGitRepositoryPath() != null) {
                oldGitRepositoryPath = branch.getGitRepositoryPath();
            } else {
                branch.setGitRepositoryPath("");
                branch.setGitRepositoryStatus(GitRepositoryStatus.UNINITIALIZED);
            }
            projectEJB.checkAdminRights(branch.getProject().getId(), httpSessionBean.getSysUser());
        }

        initJobs();
        initServers();
        initProducts();
        initVerifications();
        initVerificationConfRows();
        initTemplates();
        initProjects();
        initSelectedProject();
    }

    private void initVerifications() {
        verifications = new ArrayList<Verification>();
        if (branch.getProject() != null) {
            verifications = projectEJB.getVerifications(branch.getProject().getId());
        }
    }

    private void initProducts() {
        products = new ArrayList<Product>();
        if (branch.getProject() != null) {
            products = projectEJB.getProducts(branch.getProject().getId());
        }
    }

    private void initVerificationConfRows() {
        List<ProjectVerificationConf> projectConfs = new ArrayList<ProjectVerificationConf>();
        List<BranchVerificationConf> branchConfs = new ArrayList<BranchVerificationConf>();
        if (branch != null) {
            // Get branch level verification configurations
            branchConfs = branchEJB.getVerificationConfs(branch.getId());

            // Get project level verification configurations
            if (branch.getProject() != null) {
                projectConfs = projectEJB.getVerificationConfs(branch.getProject().getId());
            }
        }
        verificationConfRows = UIVerificationConfUtil.populateBranchVerificationConfRows(products,
                verifications, projectConfs, branchConfs);

    }

    private void initJobs() throws NotFoundException {
        jobs = new DualListModel<Job>();

        List<Job> freeJobs = jobEJB.getUnassignedJobs();
        jobs.setSource(freeJobs);

        if (branch.getId() == null) {
            return;
        }

        List<Job> branchJobs = branchEJB.getJobs(branch.getId());
        jobs.setTarget(branchJobs);
    }

    private void initServers() throws NotFoundException {
        servers = new DualListModel<CIServer>();
        List<CIServer> allServers = serverEJB.readAll();
        if (branch.getId() == null) {
            servers.setSource(allServers);
            return;
        }
        List<CIServer> branchServers = branchEJB.getCIServers(branch.getId());
        allServers.removeAll(branchServers);
        servers.setSource(allServers);
        servers.setTarget(branchServers);
    }

    private void initTemplates() {
        templates = templateEJB.readAll();
    }

    private void initProjects() {
        if (httpSessionBean.isAdmin()) {
            projects = projectEJB.readAll();
            return;
        }

        projects = sysUserEJB.getProjectAdminAccess(httpSessionBean.getSysUserId());
    }

    private void initSelectedProject() {
        if (branch.getProject() != null) {
            selectedProject = branch.getProject();
            return;
        }

        if (StringUtils.isEmpty(projectId)) {
            return;
        }

        for (Project p : projects) {
            if (p.getId().toString().equals(projectId)) {
                selectedProject = p;
                return;
            }
        }
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public String getBranchId() {
        return branchId;
    }

    public DualListModel<Job> getJobs() {
        return jobs;
    }

    public void setJobs(DualListModel<Job> jobs) {
        this.jobs = jobs;
    }

    public DualListModel<CIServer> getServers() {
        return servers;
    }

    public void setServers(DualListModel<CIServer> servers) {
        this.servers = servers;
    }

    public String save() {
        log.debug("Save triggered!");

        if (selectedProject == null && httpSessionBean.isProjectAdmin()) {
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "You have to define the project for the branch!", "");
            return null;
        }

        String action = null;
        Long pId = null;

        try {
            sanitizeGitRepositoryPath();
            if (branch.getId() != null) {
                log.debug("Updating existing branch {}", branch);
                //Check if branch type has changed or git repository path has changed.
                if (!isBranchUsingGit()) {
                    branch.setGitRepositoryPath("");
                    branch.setGitRepositoryStatus(GitRepositoryStatus.UNINITIALIZED);
                    branch = branchEJB.update(branch, getSelectedBranchVerificationConfs());
                } else if (isGitRepositoryPathChanged()) {
                    branch.setGitRepositoryStatus(GitRepositoryStatus.UNINITIALIZED);
                    branch = branchEJB.update(branch, getSelectedBranchVerificationConfs());
                } else {
                    //TODO: change to update() when branch status info is separated to another table
                    branch = branchEJB.updateFromUI(branch, getSelectedBranchVerificationConfs());
                }
            } else {
                log.debug("Saving new branch!");
                if (!isBranchUsingGit()) {
                    branch.setGitRepositoryPath("");
                    branch.setGitRepositoryStatus(GitRepositoryStatus.UNINITIALIZED);
                }
                branchEJB.create(branch, getSelectedBranchVerificationConfs());
            }
            branchEJB.setCIServers(branch.getId(), servers.getTarget());
            branchEJB.setJobs(branch.getId(), jobs.getTarget());

            // From dropdown
            if (selectedProject != null) {
                pId = selectedProject.getId();
            }

            // From URL
            if (projectId != null) {
                pId = Long.parseLong(projectId);
            }

            // Add branch to project
            if (pId != null) {
                projectEJB.addBranch(pId, branch);
            }

            if (projectId == null) {
                action = "branches?faces-redirect=true";
            } else {
                action = "projectEditor?faces-redirect=true&projectId=" + projectId;
            }
        } catch (NotFoundException nfe) {
            log.warn("Can not save branch {}! Cause: {}", branch, nfe.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Branch could not be saved!", "");
        }

        return action;
    }

    public String cancelEdit() {
        if (projectId == null) {
            return "branches?faces-redirect=true";
        } else {
            return "projectEditor?faces-redirect=true&projectId=" + projectId;
        }
    }

    public List<Product> getProducts() {
        return products;
    }

    public List<Verification> getVerifications() {
        return verifications;
    }

    public List<VerificationConfRow> getVerificationConfRows() {
        return verificationConfRows;
    }

    public void updateVerificationConfTable(ActionEvent event) throws NotFoundException {
        initVerificationConfRows();
    }

    private List<BranchVerificationConf> getSelectedBranchVerificationConfs() {
        List<BranchVerificationConf> confs = new ArrayList<BranchVerificationConf>();
        for (VerificationConfRow row : verificationConfRows) {
            for (VerificationConfCell cell : row.getCells()) {
                if (!cell.isSelected()) {
                    continue;
                }
                BranchVerificationConf conf = new BranchVerificationConf();
                conf.setBranch(branch);
                Product product = new Product();
                product.setId(cell.getProductId());
                conf.setProduct(product);
                Verification verification = new Verification();
                verification.setId(cell.getVerificationId());
                conf.setVerification(verification);

                confs.add(conf);
            }
        }
        return confs;
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

    public boolean isGitRepositoryPathChanged() {
        return !oldGitRepositoryPath.equals(branch.getGitRepositoryPath());
    }

    public boolean isBranchUsingGit() {
        if (branch == null || branch.getType() == null) {
            return false;
        }

        if (branch.getType().getRepositoryType() == RepositoryType.GIT) {
            return true;
        }

        return false;
    }

    public String getGitRepositoryStatus() {
        if (branch == null || branch.getGitRepositoryStatus() == null || isGitRepositoryPathChanged() || branch.getGitRepositoryStatus().equals(GitRepositoryStatus.UNINITIALIZED)) {
            return NOT_CLONED_MESSAGE;
        } else if (branch.getGitRepositoryStatus() == GitRepositoryStatus.CLONING) {
            return CLONE_ONGOING_MESSAGE;
        } else {
            return CLONE_OK_MESSAGE;
        }
    }

    public void gitRepositoryPathChanged() {
        if (branch.getGitRepositoryStatus() != GitRepositoryStatus.UNINITIALIZED && isGitRepositoryPathChanged()) {
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Git repository path has changed. ", "If current information is saved git repository will be set uninitialized.");
        }
    }

    public void branchTypeChanged() {
        if (branch == null || branch.getType() == null) {
            return;
        }
        branch.setGitRepositoryPath("");
        if (isBranchUsingGit()) {
            gitRepositoryPathChanged();
        }
    }

    private void sanitizeGitRepositoryPath() {
        if (branch == null) {
            return;
        }
        String gitRepositoryPath = branch.getGitRepositoryPath();
        if (StringUtils.isEmpty(gitRepositoryPath)) {
            return;
        }
        branch.setGitRepositoryPath(gitRepositoryPath.trim());
    }

    public List<Template> getTemplates() {
        return templates;
    }

    public void setTemplates(List<Template> templates) {
        this.templates = templates;
    }

    public boolean showTemplate() {
        if (branch == null || branch.getType() == BranchType.TOOLBOX
                || branch.getType() == BranchType.DRAFT) {
            return false;
        }

        return true;
    }

    public Project getSelectedProject() {
        return selectedProject;
    }

    public void setSelectedProject(Project selectedProject) {
        this.selectedProject = selectedProject;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }
}
