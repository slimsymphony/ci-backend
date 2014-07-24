/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.model.search;

import com.nokia.ci.ejb.model.BaseEntity;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ui.bean.annotation.SearchResult;

/**
 *
 * @author hhellgre
 */
@SearchResult(type = Change.class, prefix = "change")
public class ChangeSearchResult extends BaseSearchResult {

    private static final String urlBase = "/secure/pages/changeDetails.xhtml?commitId=";

    public ChangeSearchResult(BaseEntity entity) {
        super(entity);
        Change change = (Change) entity;
        url = urlBase + change.getCommitId();
        header = "Change " + change.getCommitId();
        description = change.getAuthorName() + " <" + change.getAuthorEmail() + "> - " + change.getMessage();
    }
}
