package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.ProjectGroupEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.ProjectGroup;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 10/10/12 Time: 9:47 AM To change
 * this template use File | Settings | File Templates.
 */
@Named
@ViewScoped
public class ProjectGroupBean extends AbstractUIBaseBean {

    private static Logger log = LoggerFactory.getLogger(ProjectGroupBean.class);
    private ProjectGroup projectGroup;
    @Inject
    private ProjectGroupEJB projectGroupEJB;

    @Override
    protected void init() throws NotFoundException {
        String projectGroupId = getQueryParam("projectGroupId");
        if (projectGroupId == null) {
            projectGroup = new ProjectGroup();
            return;
        }
        log.debug("Finding projectGroup {} for editing!", projectGroupId);
        projectGroup = projectGroupEJB.read(Long.parseLong(projectGroupId));
    }

    public ProjectGroup getProjectGroup() {
        return projectGroup;
    }

    public void setProjectGroup(ProjectGroup projectGroup) {
        this.projectGroup = projectGroup;
    }

    public String save() {
        log.debug("Save triggered!");

        try {
            if (projectGroup.getId() != null) {
                log.debug("Updating existing projectGroup {}", projectGroup);
                projectGroupEJB.update(projectGroup);
            } else {
                log.debug("Saving new projectGroup!");
                projectGroup.setOrder(projectGroupEJB.getMaxOrder() + 1);
                projectGroupEJB.create(projectGroup);
            }
            return "projectGroups?faces-redirect=true";
        } catch (NotFoundException nfe) {
            log.warn("Can not save projectGroup {}! Cause: {}", projectGroup, nfe.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "ProjectGroup could not be saved!", "");
        }

        return null;
    }

    public String cancelEdit() {
        return "projectGroups?faces-redirect=true";
    }
}