package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.BranchEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.Job;

import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managed bean class for branches.
 *
 * @author jajuutin
 */
@Named
public class BranchesBean extends DataFilterBean<Branch> {

    private static Logger log = LoggerFactory.getLogger(VerificationsBean.class);
    private List<Branch> branches;
    @Inject
    private BranchEJB branchEJB;
    @Inject
    private HttpSessionBean httpSessionBean;

    @Override
    protected void init() {
        initBranches();
    }

    public List<Branch> getBranches() {
        return branches;
    }

    public void delete(Branch branch) {
        log.info("Deleting branch {}", branch);
        try {
            String name = branch.getName();
            branchEJB.setJobs(branch.getId(), new ArrayList<Job>());
            branchEJB.delete(branch);
            branches.remove(branch);
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "Branch " + name + " was deleted.");
        } catch (NotFoundException ex) {
            log.warn("Deleting branch {} failed! Cause: {}", branch, ex.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Delete branch failed!", "Selected branch could not be deleted!");
        }
    }

    private void initBranches() {
        branches = new ArrayList<Branch>();
        if (httpSessionBean.isAdmin()) {
            branches = branchEJB.readAll();
            return;
        }

        List<Branch> allBranches = branchEJB.readAll();
        for (Branch b : allBranches) {
            if (b.getProject() == null) {
                continue;
            }

            if (httpSessionBean.hasAdminAccessToProject(b.getProject().getId())) {
                branches.add(b);
            }
        }
    }
}
