package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.BranchEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.exception.UnauthorizedException;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.ProjectAnnouncement;
import com.nokia.ci.ejb.model.ProjectExternalLink;
import com.nokia.ci.ejb.model.Widget;
import com.nokia.ci.ui.exception.QueryParamException;
import com.nokia.ci.ui.model.AnnouncementModel;
import com.nokia.ci.ui.widget.ProjectJobsWidget;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for single project.
 *
 * @author vrouvine
 */
@Named
@ViewScoped
public class ProjectBean extends AbstractUIBaseBean {

    private static final Logger log = LoggerFactory.getLogger(ProjectBean.class);
    private Project project;
    private List<Job> jobs;
    private List<ProjectExternalLink> links;
    @Inject
    ProjectEJB projectEJB;
    @Inject
    BranchEJB branchEJB;
    @Inject
    JobEJB jobEJB;
    @Inject
    private HttpSessionBean httpSessionBean;
    @Inject
    SysUserEJB sysUserEJB;
    private AnnouncementModel announcementModel;

    @Override
    protected void init() throws NotFoundException, UnauthorizedException, QueryParamException {
        String cachedProjectId = getQueryParam("cachedProjectId");

        String projectId;

        if (cachedProjectId == null) {
            projectId = getMandatoryQueryParam("projectId");
        } else {
            projectId = cachedProjectId;
        }

        if (!httpSessionBean.hasAccessToProject(Long.parseLong(projectId))) {
            throw new UnauthorizedException();
        }

        log.debug("Finding project {} for editing!", projectId);
        project = projectEJB.read(Long.parseLong(projectId));
        initLinks();
        initAnnouncementModel();
    }

    public void pinToMyToolbox() {
        try {
            if (project == null) {
                return;
            }

            ProjectJobsWidget wgt = new ProjectJobsWidget(project.getId(), project.getDisplayName() + " verifications");
            Widget jobWidget = wgt.getPersistentWidget();
            sysUserEJB.addWidget(httpSessionBean.getSysUserId(), jobWidget);

            addMessage(FacesMessage.SEVERITY_INFO,
                    "Project verifications added", "Project verifications is now on your toolbox");
        } catch (NotFoundException ex) {
            log.error("Could not bind project {} verifications to toolbox", project);
        }
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<Job> getJobs() {
        // Needs to be initialized here or otherwise the deleting won't work
        // through jobDatatable component
        initJobs();
        return jobs;
    }

    public List<ProjectExternalLink> getLinks() {
        return links;
    }

    public AnnouncementModel getAnnouncementModel() {
        return announcementModel;
    }

    public HttpSessionBean getHttpSessionBean() {
        return httpSessionBean;
    }

    public void setHttpSessionBean(HttpSessionBean httpSessionBean) {
        this.httpSessionBean = httpSessionBean;
    }

    private void initJobs() {
        jobs = new ArrayList<Job>();
        if (project.getId() == null) {
            return;
        }

        try {
            List<Branch> branches = projectEJB.getBranches(project.getId());
            for (Branch b : branches) {
                jobs.addAll(branchEJB.getJobs(b.getId(),
                        httpSessionBean.getSysUserId()));
            }
        } catch (NotFoundException ex) {
            log.warn("Can not fetch jobs for project {}", project, ex);
            jobs.clear();
        }
    }

    private void initLinks() {
        links = new ArrayList<ProjectExternalLink>();
        if (project.getId() == null) {
            return;
        }

        links.addAll(projectEJB.getExternalLinks(project.getId()));
    }

    private void initAnnouncementModel() {
        List<ProjectAnnouncement> appropriateAnnouncement = projectEJB.getProjectAnnouncements(project.getId());
        announcementModel = new AnnouncementModel(appropriateAnnouncement);
    }
}
