/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.BuildEJB;
import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.BuildVerificationConfEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.BranchType;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildCustomParameter;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildPhase;
import com.nokia.ci.ejb.model.BuildResultDetailsParam;
import com.nokia.ci.ejb.model.BuildVerificationConf;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.VerificationTargetPlatform;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@Named
@RequestScoped
public class BuildDatatableBean extends AbstractUIBaseBean {

    private static final Logger log = LoggerFactory.getLogger(BuildDatatableBean.class);
    @Inject
    JobEJB jobEJB;
    @Inject
    BuildEJB buildEJB;
    @Inject
    BuildGroupEJB buildGroupEJB;
    @Inject
    private HttpSessionBean httpSessionBean;
    @Inject
    BuildVerificationConfEJB buildVerificationConfEJB;
    private Map<Build, String> buildCustomParameters;
    private List<Build> buildGroupVerifications;

    public BuildDatatableBean() {
        buildCustomParameters = new HashMap<Build, String>();
    }

    public void updateBuildGroupVerifications(BuildGroup buildGroup) {
        if (buildGroup == null) {
            return;
        }

        long startTime = System.currentTimeMillis();
        log.debug("Init builds from build group {}", buildGroup);

        List<Build> builds = buildGroupEJB.getBuilds(buildGroup.getId());
        // Check that verification has VerificationTargetPlatform.INDEVICE
        Iterator<Build> it = builds.iterator();
        while (it.hasNext()) {
            Build build = it.next();
            if (build.getBuildVerificationConf() == null
                    || build.getBuildVerificationConf().getVerificationTargetPlatform() != VerificationTargetPlatform.INDEVICE
                    || build.getPhase() != BuildPhase.FINISHED) {
                it.remove();
            }
        }

        log.debug("Initialized builds from build group in {}ms", System.currentTimeMillis() - startTime);
        this.buildGroupVerifications = builds;
    }

    public List<Build> getBuildGroupVerifications() {
        return buildGroupVerifications;
    }

    public boolean isTestResultsAvailable(BuildGroup buildGroup) throws NotFoundException {
        boolean returnValue = false;
        if (buildGroup == null) {
            return false;
        }

        if (buildGroup.getJob() == null) {
            return false;
        }

        Job verification = jobEJB.read(buildGroup.getJob().getId());

        if (verification.getBranch() == null) {
            return false;
        }

        Branch b = verification.getBranch();
        if (b.getProject() == null) {
            return false;
        }

        if (!httpSessionBean.hasAccessToArtifactsAndReports(b.getProject().getId())) {
            return false;
        }

        long startTime = System.currentTimeMillis();

        log.debug("Check if build has test results from build group {}", buildGroup);

        // This is hack solution! Find out better solution when test results are available from python server
        List<BuildResultDetailsParam> results = buildGroupEJB.getBuildResultDetailsParams(buildGroup.getId());
        for (BuildResultDetailsParam r : results) {
            if (r.getParamKey() != null && r.getParamKey().equals("CI20_HAS_TEST_RESULTS")) {
                if (r.getParamValue() != null && r.getParamValue().equalsIgnoreCase("TRUE")) {
                    returnValue = true;
                    break;
                }
            }
        }

        log.debug("Checked test results from build group in {}ms", System.currentTimeMillis() - startTime);

        return returnValue;
    }

    public void retriggerBuild(BuildGroup buildGroup) throws NotFoundException {
        if (!httpSessionBean.isLoggedIn()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Retrigger failed.", "You have to login first!");
            return;
        }

        if (!isOwnBuild(buildGroup)) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Retrigger failed.", "You do not have priviliges to retrigger this build!");
            return;
        }

        List<Change> changes = buildGroupEJB.getChanges(buildGroup.getId());

        if (!changes.isEmpty()) {
            try {
                buildGroupEJB.retrigger(buildGroup.getId());
                addMessage(FacesMessage.SEVERITY_INFO,
                        "Verification retriggered!", "The verification was succesfully retriggered");
            } catch (Exception ex) {
                addMessage(FacesMessage.SEVERITY_WARN,
                        "Retrigger failed.", "There was a problem in the system. Please try again or contact support.");
            }
        } else {
            log.warn("Retriggering failed. No changes found for build {}", buildGroup);
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Retrigger failed.", "No recorded changes found. Please, use Jenkins for retriggering the change if possible.");
        }
    }

    public Boolean isOwnBuild(BuildGroup buildGroup) throws NotFoundException {
        if (buildGroup == null) {
            return false;
        }

        if (httpSessionBean.hasAdminAccessToProject(buildGroup.getProjectId())) {
            return true;
        }

        Job verification = jobEJB.read(buildGroup.getJob().getId());
        if (verification == null || verification.getBranch() == null) {
            return false;
        }

        BranchType type = verification.getBranch().getType();

        if (type != BranchType.SINGLE_COMMIT && type != BranchType.TOOLBOX && type != BranchType.DRAFT) {
            return false;
        }

        List<Change> changes = buildGroupEJB.getChanges(buildGroup.getId());

        if (changes.isEmpty()) {
            return false;
        }

        for (Change c : changes) {
            if (c.getAuthorEmail().equals(httpSessionBean.getSysUserEmail())) {
                return true;
            }
        }

        return false;
    }

    public String getCustomParameterStringForBuild(Build b) {
        if (b == null) {
            return "";
        }

        String s = buildCustomParameters.get(b);
        if (s == null) {
            BuildVerificationConf buildVerificationConf = b.getBuildVerificationConf();
            if (buildVerificationConf == null) {
                return "";
            }

            List<BuildCustomParameter> customParameters = buildVerificationConfEJB.getCustomParameters(buildVerificationConf.getId());
            if (customParameters == null || customParameters.isEmpty()) {
                return "";
            }

            long startTime = System.currentTimeMillis();
            log.debug("Formating custom parameters for {}", b);

            ListIterator<BuildCustomParameter> listIterator = customParameters.listIterator();
            StringBuilder sb = new StringBuilder();
            while (listIterator.hasNext()) {
                BuildCustomParameter param = listIterator.next();
                if (listIterator.previousIndex() > 0 && sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(param.getParamKey()).append("=").append(param.getParamValue());
            }
            s = sb.toString();
            buildCustomParameters.put(b, s);
            log.debug("Formating done in {}ms", System.currentTimeMillis() - startTime);
        }
        return s;
    }

    @Override
    protected void init() throws Exception {
    }
}
