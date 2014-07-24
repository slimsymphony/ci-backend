/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.model.search;

import com.nokia.ci.ejb.model.BaseEntity;
import com.nokia.ci.ejb.model.CIFaq;
import com.nokia.ci.ui.bean.annotation.SearchResult;

/**
 *
 * @author hhellgre
 */
@SearchResult(type = CIFaq.class, prefix = "faq")
public class FaqSearchResult extends BaseSearchResult {

    private static final String urlBase = "/secure/pages/faq.xhtml?id=";

    public FaqSearchResult(BaseEntity entity) {
        super(entity);
        CIFaq faq = (CIFaq) entity;
        url = urlBase + faq.getId();
        header = "FAQ: " + faq.getQuestion();
        String answer = faq.getAnswer();
        int maxLength = (answer.length() < 255) ? answer.length() : 255;
        description = answer.substring(0, maxLength) + "...";
    }
}
