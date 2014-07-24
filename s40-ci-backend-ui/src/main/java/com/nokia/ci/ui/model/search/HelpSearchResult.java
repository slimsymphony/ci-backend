/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.model.search;

import com.nokia.ci.ejb.model.BaseEntity;
import com.nokia.ci.ejb.model.CIHelpTopic;
import com.nokia.ci.ui.bean.annotation.SearchResult;

/**
 *
 * @author hhellgre
 */
@SearchResult(type = CIHelpTopic.class, prefix = "help")
public class HelpSearchResult extends BaseSearchResult {

    private static final String urlBase = "/secure/pages/help.xhtml?id=";

    public HelpSearchResult(BaseEntity entity) {
        super(entity);
        CIHelpTopic help = (CIHelpTopic) entity;
        url = urlBase + help.getId();
        header = "Help: " + help.getTopic();
        String content = help.getContent();
        int maxLength = (content.length() < 255) ? content.length() : 255;
        description = content.substring(0, maxLength) + "...";
    }
}
