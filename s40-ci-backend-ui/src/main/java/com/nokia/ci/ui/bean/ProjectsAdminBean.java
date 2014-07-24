package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ui.model.GroupedProjectModel;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for projects.
 *
 * @author vrouvine
 */
@Named
@ViewScoped
public class ProjectsAdminBean extends AbstractUIBaseBean {

    private static final Logger log = LoggerFactory.getLogger(ProjectsAdminBean.class);
    private List<Project> projects;
    @Inject
    private ProjectEJB projectEJB;
    private GroupedProjectModel groupedProjectModel;
    @Inject
    private HttpSessionBean httpSessionBean;
    private Long userProject;
    private Long userPrjGroup;

    @Override
    protected void init() throws NotFoundException {
        initProjects();
        groupedProjectModel = new GroupedProjectModel(getProjects());
    }

    public List<Project> getProjects() {
        return projects;
    }

    public GroupedProjectModel getGroupedProjectModel() {
        return groupedProjectModel;
    }

    @RolesAllowed({"SYSTEM_ADMIN", "PROJECT_ADMIN"})
    public void delete(Project p) {
        log.info("Deleting project {}", p);
        try {
            String name = p.getDisplayName();
            projectEJB.delete(p);
            projects.remove(p);
            groupedProjectModel = new GroupedProjectModel(getProjects());
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "Project " + name + " was deleted.");
        } catch (NotFoundException ex) {
            log.warn("Deleting project {} failed! Cause: {}", p, ex.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Delete project failed!", "Selected project could not be deleted!");
        }
    }

    private void initProjects() {
        List<Project> allProjects = projectEJB.readAll();
        projects = new ArrayList<Project>();
        for (Project p : allProjects) {
            if (httpSessionBean.hasAdminAccessToProject(p.getId())) {
                projects.add(p);
            }
        }
    }

    public void clearUserProject() {
        this.userProject = null;
    }

    public Long getUserProject() {
        return userProject;
    }

    public void setUserProject(Long userProject) {
        this.userProject = userProject;
    }

    public Long getUserPrjGroup() {
        return userPrjGroup;
    }

    public void setUserPrjGroup(Long userPrjGroup) {
        this.userPrjGroup = userPrjGroup;
    }
}
