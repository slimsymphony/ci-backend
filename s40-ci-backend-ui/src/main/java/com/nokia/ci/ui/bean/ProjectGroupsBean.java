package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.ProjectGroupEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.ProjectGroup;
import java.util.Collections;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 11/9/12 Time: 11:19 AM To change
 * this template use File | Settings | File Templates.
 */
@Named
public class ProjectGroupsBean extends DataFilterBean<ProjectGroup> {

    private static Logger log = LoggerFactory.getLogger(ProjectGroupsBean.class);
    private List<ProjectGroup> projectGroups;
    @Inject
    private ProjectGroupEJB projectGroupEJB;

    @Override
    protected void init() {
        initProjectGroups();
    }

    private void initProjectGroups() {
        projectGroups = projectGroupEJB.readAll();
        Collections.sort(projectGroups);
    }

    public List<ProjectGroup> getProjectGroups() {
        return projectGroups;
    }

    public void delete(ProjectGroup projectGroup) {
        log.info("Deleting projectGroup {}", projectGroup);
        try {
            Long id = projectGroup.getId();
            projectGroupEJB.delete(projectGroup);
            projectGroups.remove(projectGroup);
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "ProjectGroup " + id + " was deleted.");
        } catch (NotFoundException ex) {
            log.warn("Deleting ProjectGroup {} failed! Cause: {}", projectGroup, ex.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Delete ProjectGroup failed!", "Selected ProjectGroup could not be deleted!");
        }
    }
}
