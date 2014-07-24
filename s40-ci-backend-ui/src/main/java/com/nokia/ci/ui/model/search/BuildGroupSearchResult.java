/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.model.search;

import com.nokia.ci.ejb.model.BaseEntity;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ui.bean.annotation.SearchResult;

/**
 *
 * @author hhellgre
 */
@SearchResult(type = BuildGroup.class, prefix = "build")
public class BuildGroupSearchResult extends BaseSearchResult {

    private static final String urlBase = "/secure/pages/buildDetails.xhtml?buildId=";

    public BuildGroupSearchResult(BaseEntity entity) {
        super(entity);
        BuildGroup bg = (BuildGroup) entity;
        url = urlBase + bg.getId();
        header = "Build " + bg.getId();
        description = bg.getGerritProject() + " / " + bg.getJobDisplayName() + " / " + bg.getGerritPatchSetRevision() + " / " + bg.getGerritRefSpec();
    }
}
