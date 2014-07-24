/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.bean;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.index.SearchIndexCreator;
import com.nokia.ci.ejb.model.SysConfigKey;

/**
 *
 * @author hhellgre
 */
@Named
@RequestScoped
public class SearchInputBean {

    @Inject
    private SysConfigEJB sysConfigEJB;
    
    private String query;
    private boolean searchingEnabled = true;

    @PostConstruct
    public void init() {
        searchingEnabled = Boolean.valueOf(sysConfigEJB.getValue(SysConfigKey.ENABLE_SEARCH_AND_INDEXING, "true"));
    }
    public String search() {
        return "/secure/pages/search?faces-redirect=true&q=" + query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public boolean isSearchingEnabled() {
        return searchingEnabled;
    }
}
