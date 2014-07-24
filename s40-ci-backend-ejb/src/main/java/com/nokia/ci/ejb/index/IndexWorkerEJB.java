package com.nokia.ci.ejb.index;

import com.nokia.ci.ejb.model.BaseEntity;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.jboss.ejb3.annotation.TransactionTimeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author vrouvine
 */
@Stateless
@LocalBean
@TransactionTimeout(value = 5, unit = TimeUnit.HOURS)
public class IndexWorkerEJB {
    
    private static final Logger log = LoggerFactory.getLogger(IndexWorkerEJB.class);
    
    private static final int DEFAULT_BATCH_SIZE = 100;
    
    @PersistenceContext(unitName = "NokiaCI-PU")
    EntityManager em;

    @Asynchronous
    public Future<Long> doIndex(Class<? extends BaseEntity> clazz) {
        log.info("** Indexing {} **", clazz);
        long startTime = System.currentTimeMillis();
        
        Session session = (Session) em.getDelegate();
        FullTextSession fullTextSession = Search.getFullTextSession(session);
        fullTextSession.setFlushMode(FlushMode.MANUAL);
        fullTextSession.setCacheMode(CacheMode.IGNORE);
        Long totalcount = (Long) fullTextSession.createCriteria(clazz, "id")
                .setProjection(Projections.count("id")).uniqueResult();
        
        ScrollableResults results = fullTextSession.createCriteria(clazz)
                .setFetchSize(DEFAULT_BATCH_SIZE)
                .scroll(ScrollMode.FORWARD_ONLY);
        int index = 0;
        while (results.next()) {
            index++;
            fullTextSession.index(results.get(0)); //index each element
            if (index % DEFAULT_BATCH_SIZE == 0) {
                fullTextSession.flushToIndexes(); //apply changes to indexes
                fullTextSession.clear(); //free memory since the queue is processed
                log.info("Indexed [{}]: rows [{}/{}] in {}ms", new Object[]{clazz, index, totalcount, (System.currentTimeMillis() - startTime)});
            }
        }
        log.info("** Indexing [{}] {} done in {}ms", new Object[] {totalcount, clazz, System.currentTimeMillis() - startTime});
        return new AsyncResult<Long>(totalcount);
    }
}
