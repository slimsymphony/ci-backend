/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.BaseEntity;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.RoleType;
import com.nokia.ci.ejb.model.SecurityEntity;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ejb.util.SearchUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.FieldCacheTermsFilter;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@Stateless
@LocalBean
public class SearchEJB implements Serializable {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SearchEJB.class);
    @PersistenceContext(unitName = "NokiaCI-PU")
    EntityManager em;
    @Resource
    protected SessionContext context;
    @Resource(lookup = "java:jboss/infinispan/cache/ci20/project-access-cache")
    private Cache<String, SysUser> sysUserCache;

    public SearchEJB() {
    }

    public List<BaseEntity> search(String query) {
        return search(query, 0, 30);
    }

    public List<BaseEntity> search(Class type, String query) {
        return search(type, query, 0, 30);
    }

    public List<BaseEntity> search(String query, int firstResult, int maxResults) {
        return search(null, query, firstResult, maxResults);
    }

    public List<BaseEntity> search(Class type, String query, int firstResult, int maxResults) {
        log.info("Searching from Lucene index {}", query);
        long startTime = System.currentTimeMillis();
        List<BaseEntity> results = null;
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);

        Class[] types = new Class[]{type};
        if (type == null) {
            types = SearchUtil.getIndexedClasses().toArray(new Class[0]);
        }

        BooleanQuery finalQuery = new BooleanQuery();
        finalQuery.setMinimumNumberShouldMatch(1);

        Query nonSecureEntityQuery = generateNonSecureEntityQuery(query, type);
        Query secureEntityQuery = generateSecureEntityQuery(query, type);

        if (nonSecureEntityQuery != null) {
            finalQuery.add(nonSecureEntityQuery, BooleanClause.Occur.SHOULD);
        }

        if (secureEntityQuery != null) {
            finalQuery.add(secureEntityQuery, BooleanClause.Occur.SHOULD);
        }

        org.hibernate.search.jpa.FullTextQuery fullTextQuery;
        fullTextQuery = fullTextEntityManager.createFullTextQuery(finalQuery, types);

        fullTextQuery.setFirstResult(firstResult);
        fullTextQuery.setMaxResults(maxResults);
        results = fullTextQuery.getResultList();
        log.info("Found {} results from Lucene index with query: '" + query + "' in {} ms", results.size(), System.currentTimeMillis() - startTime);
        return results;
    }

    private Query generateNonSecureEntityQuery(String query, Class type) {
        BooleanQuery q = new BooleanQuery();
        q.setMinimumNumberShouldMatch(1);

        if (type == Project.class) {
            return null;
        }

        if (type == null) {
            Set<Class<?>> classes = SearchUtil.getIndexedNonSecurityClasses();
            for (Class c : classes) {
                BooleanQuery q1 = generateWordsQuery(c, query);
                q.add(q1, BooleanClause.Occur.SHOULD);
            }

            return q;
        }

        if (SecurityEntity.class.isAssignableFrom(type)) {
            return null;
        }

        q = generateWordsQuery(type, query);
        return q;
    }

    private Query generateSecureEntityQuery(String query, Class type) {
        BooleanQuery q = new BooleanQuery();
        q.setMinimumNumberShouldMatch(1);

        // Restrict search results based on users project access
        SysUser user = sysUserCache.get(context.getCallerPrincipal().getName());
        if (user == null) {
            return null;
        }

        boolean isAdmin = false;
        if (user.getUserRole().equals(RoleType.SYSTEM_ADMIN)) {
            isAdmin = true;
        }

        if (type == null) {
            Set<Class<?>> classes = SearchUtil.getIndexedSecurityClasses();

            for (Class c : classes) {
                BooleanQuery q1 = generateWordsQuery(c, query);
                q.add(q1, BooleanClause.Occur.SHOULD);
            }
        } else {
            if (!SecurityEntity.class.isAssignableFrom(type) && type != Project.class) {
                return null;
            }

            BooleanQuery q1 = generateWordsQuery(type, query);
            q.add(q1, BooleanClause.Occur.SHOULD);
        }

        if (!isAdmin) {
            Set<Project> projectAccess = new HashSet(user.getProjectAccess());
            projectAccess.addAll(user.getProjectAdminAccess());

            if (projectAccess.isEmpty()) {
                return null;
            }

            List<String> projectAccessStr = new ArrayList<String>();
            for (Project p : projectAccess) {
                projectAccessStr.add(p.getId().toString());
            }

            FieldCacheTermsFilter pf = new FieldCacheTermsFilter("projectId", projectAccessStr.toArray(new String[0]));
            ConstantScoreQuery fq = new ConstantScoreQuery(pf);
            q.add(fq, BooleanClause.Occur.MUST);
        }

        return q;
    }

    private BooleanQuery generateWordsQuery(Class type, String query) {
        BooleanQuery q = new BooleanQuery();
        Set<String> fields = SearchUtil.getIndexedFields(type);
        for (String field : fields) {
            // Loop through search words and wildcard them
            String[] queryWords = query.split("\\s+");
            for (String word : queryWords) {
                WildcardQuery fq = new WildcardQuery(new Term(field, "*" + word + "*"));
                q.add(fq, BooleanClause.Occur.SHOULD);
            }

            // Phrase should match more often than single words
            if (queryWords.length > 1) {
                PhraseQuery fq = new PhraseQuery();
                fq.add(new Term(field, query));
                q.add(fq, BooleanClause.Occur.SHOULD);
            }
        }
        return q;
    }
}
