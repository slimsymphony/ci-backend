package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.BranchEJB;
import com.nokia.ci.ejb.GerritEJB;
import com.nokia.ci.ejb.ProductEJB;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.ProjectExternalLinkEJB;
import com.nokia.ci.ejb.ProjectGroupEJB;
import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.VerificationEJB;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.Gerrit;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.Product;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.ProjectExternalLink;
import com.nokia.ci.ejb.model.ProjectGroup;
import com.nokia.ci.ejb.model.ProjectVerificationConf;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ejb.model.Verification;
import com.nokia.ci.ui.model.VerificationConfCell;
import com.nokia.ci.ui.model.VerificationConfRow;
import com.nokia.ci.ui.util.UIVerificationConfUtil;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.DualListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for project editing.
 *
 * @author vrouvine
 */
@Named
@ViewScoped
public class ProjectEditorBean extends AbstractUIBaseBean {

    private static final Logger log = LoggerFactory.getLogger(ProjectEditorBean.class);
    private Project project;
    private DualListModel<Branch> branches = new DualListModel<Branch>();
    private DualListModel<Product> products = new DualListModel<Product>();
    private DualListModel<Verification> verifications = new DualListModel<Verification>();
    private List<VerificationConfRow> verificationConfRows = new ArrayList<VerificationConfRow>();
    private List<ProjectExternalLink> links = new ArrayList<ProjectExternalLink>();
    private List<ProjectGroup> projectGroups = new ArrayList<ProjectGroup>();
    private ProjectGroup selectedProjectGroup;
    private List<Gerrit> gerrits = new ArrayList<Gerrit>();
    private Gerrit selectedGerrit;
    private ProjectExternalLink newLink;
    private DualListModel<SysUser> admins = new DualListModel<SysUser>();
    @Inject
    private ProjectEJB projectEJB;
    @Inject
    private ProjectExternalLinkEJB projectExternalLinkEJB;
    @Inject
    ProductEJB productEJB;
    @Inject
    SysUserEJB sysUserEJB;
    @Inject
    HttpSessionBean httpSessionBean;
    @Inject
    BranchEJB branchEJB;
    @Inject
    VerificationEJB verificationEJB;
    @Inject
    ProjectGroupEJB projectGroupEJB;
    @Inject
    GerritEJB gerritEJB;

    /**
     * Creates a new instance of ProjectEditorBean
     */
    public ProjectEditorBean() {
    }

    @Override
    protected void init() throws BackendAppException {
        String projectId = getQueryParam("projectId");
        project = new Project();
        newLink = new ProjectExternalLink();
        if (projectId != null) {
            log.debug("Finding project {} for editing!", projectId);
            project = projectEJB.read(Long.parseLong(projectId));
            projectEJB.checkAdminRights(project.getId(), httpSessionBean.getSysUser());
        }
        initBranches();
        initProducts();
        initVerifications();
        initVerificationConfRows();
        initProjectGroups();
        initGerrits();
        initLinks();
        initAdmins();
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public DualListModel<Branch> getBranches() {
        return branches;
    }

    public void setBranches(DualListModel<Branch> branches) {
        this.branches = branches;
    }

    public DualListModel<Product> getProducts() {
        return products;
    }

    public List<ProjectExternalLink> getLinks() {
        return links;
    }

    public void setProducts(DualListModel<Product> products) {
        this.products = products;
    }

    public DualListModel<Verification> getVerifications() {
        return verifications;
    }

    public void setVerifications(DualListModel<Verification> verifications) {
        this.verifications = verifications;
    }

    public List<VerificationConfRow> getVerificationConfRows() {
        return verificationConfRows;
    }

    public void updateVerificationConfTable(ActionEvent event) throws NotFoundException {
        initVerificationConfRows();
    }

    public void verificationsChanged(ValueChangeEvent event) throws NotFoundException {
        verifications = (DualListModel<Verification>) event.getNewValue();
        initVerificationConfRows();
    }

    public List<Gerrit> getGerrits() {
        return gerrits;
    }

    public void setGerrits(List<Gerrit> gerrits) {
        this.gerrits = gerrits;
    }

    public Gerrit getSelectedGerrit() {
        return selectedGerrit;
    }

    public void setSelectedGerrit(Gerrit selectedGerrit) {
        this.selectedGerrit = selectedGerrit;
    }

    public ProjectExternalLink getNewLink() {
        return newLink;
    }

    public void setNewLink(ProjectExternalLink newLink) {
        this.newLink = newLink;
    }

    public ProjectGroup getSelectedProjectGroup() {
        return selectedProjectGroup;
    }

    public void setSelectedProjectGroup(ProjectGroup selectedProjectGroup) {
        this.selectedProjectGroup = selectedProjectGroup;
    }

    public List<ProjectGroup> getProjectGroups() {
        return projectGroups;
    }

    public void setProjectGroups(List<ProjectGroup> projectGroups) {
        this.projectGroups = projectGroups;
    }

    public DualListModel<SysUser> getAdmins() {
        return admins;
    }

    public void setAdmins(DualListModel<SysUser> admins) {
        this.admins = admins;
    }

    public String save() {
        log.debug("Save triggered!");

        return saveChanges("projects?faces-redirect=true");
    }

    public String apply() {
        log.debug("Apply triggered!");

        String redirect = saveChanges("");
        if (redirect != null) {
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Project saved successfully!", "");
        }
        return null;
    }

    public String cancelEdit() {
        return "projects?faces-redirect=true";
    }

    public void deleteBranch() {
        Long branchId = Long.valueOf(getQueryParam("branchId"));

        try {
            branchEJB.setJobs(branchId, new ArrayList<Job>());
            Branch branch = branchEJB.read(branchId);
            branchEJB.delete(branch);

            branches.getSource().remove(branch);
            branches.getTarget().remove(branch);

            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "Branch " + branch.getName() + " was deleted.");
        } catch (NotFoundException ex) {
            // For some reason this method is called twice when deleting branch
            // This means that both "Delete branch failed" and "Operation succesful" messages are displayed
            // Removed this so it will either go away or do nothing..
        }
    }

    private String saveChanges(String redirectAddress) {
        project.setProducts(products.getTarget());
        project.setVerifications(verifications.getTarget());
        project.setAdminAccess(admins.getTarget());
        project.setLinks(links);

        try {
            if (project.getId() != null) {
                log.debug("Updating existing project {}", project);
                projectEJB.update(project, getSelectedProjectVerificationConfs());
            } else {
                log.debug("Saving new project!");
                projectEJB.create(project, getSelectedProjectVerificationConfs());
            }
            projectEJB.setBranches(project.getId(), branches.getTarget());
            projectEJB.setProjectGroup(project.getId(), selectedProjectGroup);
            projectEJB.setGerrit(project.getId(), selectedGerrit);
            projectEJB.removeJobVerifications(project.getId(), verifications.getSource());
            return redirectAddress;
        } catch (NotFoundException nfe) {
            log.warn("Can not save project {}! Cause: {}", project, nfe.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Project could not be saved!", "");
        }
        return null;
    }

    private void initBranches() {
        branches = new DualListModel<Branch>();
        List<Branch> freeBranches = branchEJB.getUnassignedBranches();
        if (project.getId() == null) {
            branches.setSource(freeBranches);
            return;
        }
        List<Branch> projectBranches = projectEJB.getBranches(project.getId());
        branches.setSource(freeBranches);
        branches.setTarget(projectBranches);
    }

    private void initProducts() {
        products = new DualListModel<Product>();
        List<Product> availableProducts = productEJB.readAll();
        if (project.getId() == null) {
            products.setSource(availableProducts);
            return;
        }
        List<Product> projectProducts = projectEJB.getProducts(project.getId());
        availableProducts.removeAll(projectProducts);
        products.setSource(availableProducts);
        products.setTarget(projectProducts);
    }

    private void initVerifications() {
        verifications = new DualListModel<Verification>();
        List<Verification> availableVerifications = verificationEJB.readAll();
        if (project.getId() == null) {
            verifications.setSource(availableVerifications);
            return;
        }
        List<Verification> projectVerifications = projectEJB.getAllVerifications(project.getId());
        availableVerifications.removeAll(projectVerifications);
        verifications.setSource(availableVerifications);
        verifications.setTarget(projectVerifications);
    }

    private void initVerificationConfRows() {
        List<ProjectVerificationConf> confs = new ArrayList<ProjectVerificationConf>();
        if (project.getId() != null) {
            confs = projectEJB.getVerificationConfs(project.getId());
        }
        verificationConfRows = UIVerificationConfUtil.populateProjectVerificationConfRows(products.getTarget(),
                verifications.getTarget(), confs);
    }

    private void initProjectGroups() {
        selectedProjectGroup = project.getProjectGroup();
        projectGroups = projectGroupEJB.readAll();
    }

    private void initLinks() {
        links = new ArrayList<ProjectExternalLink>();
        if (project.getId() == null) {
            return;
        }

        links.addAll(projectEJB.getExternalLinks(project.getId()));
    }

    private void initGerrits() {
        selectedGerrit = project.getGerrit();
        gerrits = gerritEJB.readAll();
    }

    private void initAdmins() {
        admins = new DualListModel<SysUser>();
        List<SysUser> projectAdmins = new ArrayList<SysUser>();
        if (project.getId() != null) {
            projectAdmins.addAll(projectEJB.getAdmins(project.getId()));
        } else {
            projectAdmins.add(httpSessionBean.getSysUser());
        }

        List<SysUser> allUsers = new ArrayList<SysUser>();
        allUsers.addAll(sysUserEJB.readAll());

        allUsers.removeAll(projectAdmins);
        admins.setSource(allUsers);
        admins.setTarget(projectAdmins);
    }

    private List<ProjectVerificationConf> getSelectedProjectVerificationConfs() {
        List<ProjectVerificationConf> confs = new ArrayList<ProjectVerificationConf>();
        for (VerificationConfRow row : verificationConfRows) {
            for (VerificationConfCell cell : row.getCells()) {
                if (!cell.isSelected()) {
                    continue;
                }
                ProjectVerificationConf conf = new ProjectVerificationConf();
                conf.setProject(project);
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

    public void deleteLink(ProjectExternalLink l) throws NotFoundException {
        links.remove(l);
        projectExternalLinkEJB.delete(l);
    }

    public void saveLink() throws NotFoundException {
        log.debug("Adding new link {}", newLink);

        newLink.setProject(project);
        links.add(newLink);
        newLink = new ProjectExternalLink();
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
}
