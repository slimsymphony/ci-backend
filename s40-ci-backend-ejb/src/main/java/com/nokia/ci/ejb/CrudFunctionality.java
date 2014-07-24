package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.exception.UnauthorizedException;
import com.nokia.ci.ejb.model.BaseEntity;
import com.nokia.ci.ejb.model.RoleType;
import com.nokia.ci.ejb.model.SysUser;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 */
public abstract class CrudFunctionality<T extends BaseEntity> {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(CrudFunctionality.class);
    /**
     * Must store type for entity manager find method.
     */
    protected Class<T> type;
    @PersistenceContext(unitName = "NokiaCI-PU")
    protected EntityManager em;
    @Resource
    protected SessionContext context;
    protected CriteriaBuilder cb;

    @PostConstruct
    public void init() {
        cb = em.getCriteriaBuilder();
    }

    /**
     * Void contructor needed to allow serialization of subclasses.
     */
    protected CrudFunctionality() {
        // Empty contructor.
    }

    /**
     *
     * @param type
     */
    protected CrudFunctionality(Class<T> type) {
        this.type = type;
    }

    /**
     * Create of CRUD
     */
    public void create(T object) {
        String user = getCallerUsername();
        log.info("Persisting new '{}' to database by {}",
                object.getClass().getCanonicalName(), user);
        object.setCreatedBy(user);
        em.persist(object);
        log.debug("Flushing database after persisting new '{}'",
                object.getClass().getCanonicalName());
        em.flush();
        log.debug("Completed create of new '{}' to database",
                object.getClass().getCanonicalName());
    }

    /**
     * Create of CRUD with no flushing.
     *
     * This method is similar to create but does not flush and is therefore more
     * efficient. However in this case id of new entity will not be immediatelly
     * available (it is not updated from database).
     */
    public void createNoFlush(T object) {
        String user = getCallerUsername();
        log.info("Persisting new '{}' to database by {}",
                object.getClass().getCanonicalName(), user);
        object.setCreatedBy(user);
        em.persist(object);
        log.debug("Completed create of new '{}' to database",
                object.getClass().getCanonicalName());
    }

    /**
     * Read of CRUD
     */
    public T read(Long id) throws NotFoundException {
        log.debug("Read '{}' from database using id {}",
                type.getCanonicalName(), id);

        T found = em.find(type, id);

        if (found == null) {
            throw new NotFoundException(id, type);
        }

        return found;
    }

    public T readWithLock(Long id, LockModeType lockMode) throws NotFoundException {
        log.debug("Read '{}' from database using id {} and lock mode {}", new Object[]{
            type.getCanonicalName(), id, lockMode});

        T found = em.find(type, id, lockMode);

        if (found == null) {
            throw new NotFoundException(id, type);
        }

        return found;
    }

    /**
     * Read All of ?
     */
    public List<T> readAll() {
        log.debug("Reading all '{}' from database", type.getCanonicalName());
        CriteriaQuery<T> criteria = cb.createQuery(type);
        Root<T> root = criteria.from(type);
        criteria.select(root);
        TypedQuery<T> query = em.createQuery(criteria);
        List<T> objects = query.getResultList();

        return objects;
    }

    /**
     * Update of CRUD
     */
    public T update(T object) throws NotFoundException {
        String user = getCallerUsername();
        log.info("Update {} by {}", object, user);

        T found = em.find(type, object.getId());

        if (found == null) {
            throw new NotFoundException(object.getId(), type);
        }

        object.setModifiedBy(user);
        return em.merge(object);
    }

    /**
     * Delete of CRUD
     */
    public void delete(T object) throws NotFoundException {
        log.info("Deleting {}", object);
        object = read(object.getId());
        em.remove(object);
    }

    /**
     * Perform join query for given {@link ListAttribute}.
     *
     * @param E Type of returned entities
     * @param id Parent entity id
     * @param listAttribute {@link ListAttribute} for joined attribute list.
     * @return List of E typed entities
     */
    protected <E extends BaseEntity> List<E> getJoinList(Long id, ListAttribute<T, E> listAttribute) {
        Class<E> joinClass = listAttribute.getBindableJavaType();
        if (log.isDebugEnabled()) {
            log.debug("Quering list of {} for {}", joinClass, type);
        }
        CriteriaQuery<E> query = cb.createQuery(joinClass);
        Root<T> root = query.from(type);
        ListJoin<T, E> listJoin = root.join(listAttribute);
        query.select(listJoin);
        query.where(cb.equal(root, id));

        List<E> results = em.createQuery(query).getResultList();
        if (log.isDebugEnabled()) {
            log.debug("Found {} of {} for {}", new Object[]{results.size(), joinClass, type});
        }
        return results;
    }

    /**
     * Perform join query for given {@link ListAttribute} and return count of
     * the items.
     *
     * @param E Type of counted objects
     * @param id Parent entity id
     * @param listAttribute {@link ListAttribute} for joined attribute list.
     * @return Long value of count
     */
    protected <E extends BaseEntity> Long getJoinListCount(Long id, ListAttribute<T, E> listAttribute) {
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(type);
        ListJoin<T, E> listJoin = root.join(listAttribute);
        query.select(cb.count(listJoin));
        query.where(cb.equal(root, id));

        return em.createQuery(query).getSingleResult();
    }

    /**
     * Perform search for string fields and return results
     *
     * @param E Type of searched entities
     * @param query Search query
     * @param fields Array of {@link SingularAttribute} fields in objects where
     * to search from
     * @return List of found entities
     */
    protected <E extends BaseEntity> List<T> search(String query, SingularAttribute<T, String>... fields) {
        if (StringUtils.isEmpty(query)) {
            return new ArrayList<T>();
        }

        if (fields == null || fields.length < 1) {
            return new ArrayList<T>();
        }

        List<String> attributeNames = new ArrayList<String>();
        for (SingularAttribute<T, String> field : fields) {
            attributeNames.add(field.getName());
        }

        List<T> result = new ArrayList<T>();
        try {
            FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);

            String[] attributes = attributeNames.toArray(new String[0]);
            QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(type).get();
            org.apache.lucene.search.Query q = qb.keyword().onFields(attributes).matching("*" + query + "*").createQuery();

            javax.persistence.Query persistenceQuery = fullTextEntityManager.createFullTextQuery(q, type);
            result = persistenceQuery.getResultList();
        } catch (Exception e) {
            log.warn("Could not search with query {}", query);
        } finally {
            if (result == Collections.EMPTY_LIST) {
                return new ArrayList<T>();
            }
            return result;
        }
    }

    protected String getCallerUsername() {
        Principal p = context.getCallerPrincipal();
        if (p == null) {
            return null;
        }
        return p.getName();
    }
}
