package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.ProjectGroupEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.ProjectGroup;
import java.util.Collections;
import java.util.List;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 11/12/12 Time: 2:25 PM To change
 * this template use File | Settings | File Templates.
 */
@Named
@ViewScoped
public class ProjectGroupsOrderBean extends AbstractUIBaseBean {

    private static Logger log = LoggerFactory.getLogger(ProjectGroupsBean.class);
    private List<ProjectGroup> projectGroups;
    @Inject
    private ProjectGroupEJB projectGroupEJB;

    @Override
    protected void init() {
        initProjectGroups();
    }

    public void setProjectGroups(List<ProjectGroup> projectGroups) {
        this.projectGroups = projectGroups;
    }

    public List<ProjectGroup> getProjectGroups() {
        return projectGroups;
    }

    private void initProjectGroups() {
        projectGroups = projectGroupEJB.readAll();
        Collections.sort(projectGroups);
    }

    public String save() throws NotFoundException {
        int order = 0;
        for (ProjectGroup projectGroup : projectGroups) {
            projectGroup.setOrder(order);
            projectGroupEJB.update(projectGroup);
            order++;
        }
        return "projectGroups?faces-redirect=true";
    }

    public String cancelEdit() {
        return "projectGroups?faces-redirect=true";
    }
}
