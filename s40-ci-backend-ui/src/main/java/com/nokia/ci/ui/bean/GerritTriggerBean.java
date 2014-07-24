package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.GerritEJB;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.exception.UnauthorizedException;
import com.nokia.ci.ejb.gerrit.GerritJobManagerEJB;
import com.nokia.ci.ejb.gerrit.model.GerritDetail;
import com.nokia.ci.ejb.model.Gerrit;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.JobTriggerType;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ui.exception.QueryParamException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author miikka
 */
@Named
@ViewScoped
public class GerritTriggerBean extends AbstractUIBaseBean {

    private static Logger log = LoggerFactory.getLogger(GerritTriggerBean.class);
    @Inject
    private GerritEJB gerritEJB;
    @Inject
    private SysUserEJB sysUserEJB;
    @Inject
    private GerritJobManagerEJB gerritJobManagerEJB;
    @Inject
    private HttpSessionBean httpSessionBean;
    @Inject
    private ProjectEJB projectEJB;
    private Gerrit selectedGerrit = null;
    private String query;
    private List<GerritDetail> details = new ArrayList<GerritDetail>();
    private List<GerritDetail> filteredDetails = new ArrayList<GerritDetail>();
    private GerritDetail[] selectedDetails;
    private List<Gerrit> gerrits;
    private Long projectId;
    private Project project = null;

    public GerritTriggerBean() {
    }

    @Override
    protected void init() throws NotFoundException, QueryParamException, UnauthorizedException {
        String param = getQueryParam("projectId");
        if (!StringUtils.isEmpty(param)) {
            log.debug("Trying to find project with id {}", param);
            projectId = Long.parseLong(param);
            if (!httpSessionBean.hasAccessToProject(projectId)) {
                throw new UnauthorizedException();
            }
            project = projectEJB.read(projectId);
            selectedGerrit = project.getGerrit();
        } else if (!httpSessionBean.isProjectAdmin()) {
            throw new UnauthorizedException();
        }

        if (httpSessionBean.isProjectAdmin()) {
            gerrits = gerritEJB.readAll();
        }

        query = "status:open " + httpSessionBean.getSysUserEmail();

        search();
    }

    public void search() {
        try {
            details = new ArrayList<GerritDetail>();
            if (selectedGerrit == null && httpSessionBean.isProjectAdmin()) {
                if (httpSessionBean.isAdmin()) {
                    // Admins can search from all gerrits and projects
                    for (Gerrit g : gerrits) {
                        details.addAll(gerritEJB.gerritQuery(query, g.getId()));
                    }
                } else {
                    // Project admin can search from administrated projects
                    List<Project> projects = sysUserEJB.getProjectAdminAccess(httpSessionBean.getSysUserId());
                    for (Project p : projects) {
                        if (StringUtils.isEmpty(p.getGerritProject())) {
                            continue;
                        }

                        String prjQuery = query;
                        for (Gerrit g : gerrits) {
                            if (!prjQuery.contains("project:" + p.getGerritProject())) {
                                prjQuery += " project:" + p.getGerritProject();
                            }
                            details.addAll(gerritEJB.gerritQuery(prjQuery, g.getId()));
                        }
                    }
                }
                filteredDetails = details;
                return;
            }

            if (selectedGerrit != null) {
                // Add project parameter for normal users
                if (!httpSessionBean.isProjectAdmin() && StringUtils.isNotEmpty(project.getGerritProject())) {
                    query += " project:" + project.getGerritProject();
                }

                // If project admin, allow searching administrated projects only
                if (httpSessionBean.isProjectAdmin() && !httpSessionBean.isAdmin()) {
                    List<Project> projects = sysUserEJB.getProjectAdminAccess(httpSessionBean.getSysUserId());
                    for (Project p : projects) {
                        if (StringUtils.isEmpty(p.getGerritProject())) {
                            continue;
                        }
                        String prjQuery = query;
                        if (!prjQuery.contains("project:" + p.getGerritProject())) {
                            prjQuery += " project:" + p.getGerritProject();
                        }
                        details.addAll(gerritEJB.gerritQuery(prjQuery, selectedGerrit.getId()));
                    }

                    return;
                }

                // Do not allow normal users to find without project parameter
                if (!httpSessionBean.isAdmin() && !query.contains("project:")) {
                    return;
                }

                details = gerritEJB.gerritQuery(query, selectedGerrit.getId());
                filteredDetails = details;
            }

        } catch (NotFoundException ex) {
            log.warn("Search '{}' failed from {}", query, selectedGerrit);
        }
    }

    public String trigger() {
        String action = "";

        if (!httpSessionBean.isLoggedIn()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Start failed.", "You have to login first!");
            return "login?faces-redirect=true";
        }

        if (selectedDetails.length == 0) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "No changes selected", "");
            return action;
        }

        // Start only jobs with trigger type AUTOMATIC
        List<Job> startedJobs = gerritJobManagerEJB.startJobs(Arrays.asList(selectedDetails), JobTriggerType.AUTOMATIC);
        action = "";
        log.info("Gerrit manual trigger hit for {} changes. Triggered by {}", selectedDetails.length, httpSessionBean.getSysUserLoginName());

        String msg = "No jobs matched AUTOMATIC triggering";
        if (startedJobs.size() > 0) {
            msg = startedJobs.size() + " job(s) are queued for starting:";
            log.info(msg);
            addMessage(FacesMessage.SEVERITY_INFO,
                    msg, "");

            for (Job j : startedJobs) {
                msg = "Job " + j.getDisplayName() + " is queued for starting";
                addMessage(FacesMessage.SEVERITY_INFO,
                        msg, "");
            }

            details.removeAll(new ArrayList<GerritDetail>(Arrays.asList(selectedDetails)));
        } else {
            log.info(msg);
            addMessage(FacesMessage.SEVERITY_INFO,
                    msg, "");
        }

        selectedDetails = null;

        return action;
    }

    public Project getProject() {
        return project;
    }

    public List<Gerrit> getGerrits() {
        return gerrits;
    }

    public Gerrit getSelectedGerrit() {
        return selectedGerrit;
    }

    public void setSelectedGerrit(Gerrit selectedGerrit) {
        this.selectedGerrit = selectedGerrit;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<GerritDetail> getDetails() {
        return details;
    }

    public void setDetails(List<GerritDetail> details) {
        this.details = details;
    }

    public List<GerritDetail> getFilteredDetails() {
        return filteredDetails;
    }

    public void setFilteredDetails(List<GerritDetail> filteredDetails) {
        this.filteredDetails = filteredDetails;
    }

    public GerritDetail[] getSelectedDetails() {
        return selectedDetails;
    }

    public void setSelectedDetails(GerritDetail[] selectedDetails) {
        this.selectedDetails = selectedDetails;
    }

    public Long getProjectId() {
        return projectId;
    }
}
