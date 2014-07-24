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
public class ProjectsBean extends AbstractUIBaseBean {

    private static final Logger log = LoggerFactory.getLogger(ProjectsBean.class);
    private List<Project> projects;
    @Inject
    private ProjectEJB projectEJB;
    private GroupedProjectModel groupedProjectModel;
    @Inject
    private HttpSessionBean httpSessionBean;

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

    private void initProjects() {
        List<Project> allProjects = projectEJB.readAll();
        projects = new ArrayList<Project>();
        for (Project p : allProjects) {
            if (httpSessionBean.hasAccessToProject(p.getId())) {
                projects.add(p);
            }
        }
    }
}
