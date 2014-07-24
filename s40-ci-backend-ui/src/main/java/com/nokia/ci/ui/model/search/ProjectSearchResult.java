/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.model.search;

import com.nokia.ci.ejb.model.BaseEntity;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ui.bean.annotation.SearchResult;

/**
 *
 * @author hhellgre
 */
@SearchResult(type = Project.class, prefix = "project")
public class ProjectSearchResult extends BaseSearchResult {

    private static final String urlBase = "/secure/pages/projectDetails.xhtml?projectId=";

    public ProjectSearchResult(BaseEntity entity) {
        super(entity);
        Project project = (Project) entity;
        url = urlBase + project.getId();
        header = "Project " + project.getDisplayName();
        description = project.getDescription();
    }
}
