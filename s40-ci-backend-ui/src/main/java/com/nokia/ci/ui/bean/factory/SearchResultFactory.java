/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.bean.factory;

import com.nokia.ci.ejb.model.BaseEntity;
import com.nokia.ci.ejb.model.SecurityEntity;
import com.nokia.ci.ui.bean.annotation.SearchResult;
import com.nokia.ci.ui.model.search.BaseSearchResult;
import java.lang.Class;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
public class SearchResultFactory {

    private static Logger log = LoggerFactory.getLogger(SearchResultFactory.class);

    public static BaseSearchResult createSearchResult(BaseEntity entity) {
        BaseSearchResult ret = null;

        try {
            Reflections reflections = new Reflections("com.nokia.ci.ui.model.search");
            Set<Class<?>> classes = reflections.getTypesAnnotatedWith(SearchResult.class);

            for (Class clazz : classes) {
                SearchResult ann = (SearchResult) clazz.getAnnotation(SearchResult.class);

                if (ann.type().isInstance(entity)) {
                    ret = (BaseSearchResult) clazz.getDeclaredConstructor(BaseEntity.class).newInstance(entity);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Unable to find suitable class for Search result BaseEntity " + entity.toString(), e);
        }
        return ret;
    }

    public static List<BaseSearchResult> createSearchResults(List<? extends BaseEntity> entities) {
        List<BaseSearchResult> ret = new ArrayList<BaseSearchResult>();
        for (BaseEntity entity : entities) {
            BaseSearchResult res = SearchResultFactory.createSearchResult(entity);
            if (res == null) {
                continue;
            }
            ret.add(res);
        }
        return ret;
    }
}
