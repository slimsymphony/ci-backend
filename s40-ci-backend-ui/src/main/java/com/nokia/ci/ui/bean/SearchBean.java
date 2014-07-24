package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.SearchEJB;
import com.nokia.ci.ejb.model.BaseEntity;
import com.nokia.ci.ui.bean.annotation.SearchResult;
import com.nokia.ci.ui.bean.factory.SearchResultFactory;
import com.nokia.ci.ui.model.search.BaseSearchResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

/**
 *
 * @author hhellgre
 */
@Named
@ViewScoped
public class SearchBean extends AbstractUIBaseBean {

    private String query;
    private String searchQuery;
    private Boolean hasResults = false;
    private List<BaseSearchResult> searchResults;
    private int lastResult = 0;
    private final int fetchResults = 15;
    private final int maxResults = 100;
    private Boolean hasMoreResults = true;
    private Map<String, Class> searchPrefixes;
    private static final String PREFIX_SEPARATOR_CHARACTER = ":";
    @Inject
    private SearchEJB searchEJB;

    @Override
    protected void init() {
        query = getQueryParam("q");

        if (StringUtils.isNotEmpty(query)) {
            initSearchPrefixes();
            searchResults = new ArrayList<BaseSearchResult>();
            searchQuery = query;
            find();
        }
    }

    private void initSearchPrefixes() {
        searchPrefixes = new HashMap<String, Class>();
        Reflections reflections = new Reflections("com.nokia.ci.ui.model.search");
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(SearchResult.class);

        for (Class clazz : classes) {
            SearchResult ann = (SearchResult) clazz.getAnnotation(SearchResult.class);
            String prefix = ann.prefix();
            if (StringUtils.isNotEmpty(prefix)) {
                searchPrefixes.put(prefix + PREFIX_SEPARATOR_CHARACTER, ann.type());
            }
        }
    }

    public void find() {
        if (query == null || query.isEmpty() || hasMoreResults == false
                || lastResult >= maxResults) {
            hasMoreResults = false;
            return;
        }

        Class type = getSearchType();
        List<BaseEntity> results = searchEJB.search(type, searchQuery, lastResult, fetchResults);
        if (!results.isEmpty()) {
            searchResults.addAll(SearchResultFactory.createSearchResults(results));
            hasResults = true;
            lastResult += results.size();
            if (results.size() < fetchResults) {
                hasMoreResults = false;
            }
        }
    }

    private Class getSearchType() {
        Class ret = null;
        for (Map.Entry<String, Class> entry : searchPrefixes.entrySet()) {
            if (query.startsWith(entry.getKey())) {
                searchQuery = query.replaceFirst(entry.getKey(), "");
                ret = entry.getValue();
                break;
            }
        }

        return ret;
    }

    public Boolean getHasResults() {
        return hasResults;
    }

    public List<BaseSearchResult> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<BaseSearchResult> searchResults) {
        this.searchResults = searchResults;
    }

    public Boolean getHasMoreResults() {
        return hasMoreResults;
    }

    public void setHasMoreResults(Boolean hasMoreResults) {
        this.hasMoreResults = hasMoreResults;
    }
}
