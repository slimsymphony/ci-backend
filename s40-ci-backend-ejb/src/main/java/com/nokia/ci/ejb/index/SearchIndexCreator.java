/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.index;

import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.hasingleton.AbstractHASingletonTimer;
import com.nokia.ci.ejb.model.BaseEntity;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.util.SearchUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.infinispan.Cache;

/**
 *
 * @author hhellgre
 */
@Singleton
public class SearchIndexCreator extends AbstractHASingletonTimer {

    private static Logger log = LoggerFactory.getLogger(SearchIndexCreator.class);
    private Cache<String, Boolean> cache;
    private static final String INDEXING_STARTED_CHECK = "LuceneIndexingStarted";
    private static final long TIMER_TIMEOUT_DEFAULT = 1000;
    @Resource
    TimerService timerService;
    @EJB
    private SysConfigEJB sysConfigEJB;
    @EJB
    private IndexWorkerEJB indexWorkerEJB;
    private List<Future<Long>> workers = new ArrayList<Future<Long>>();
    private long indexingStartTime = 0;

    @Override
    public void start() {
        // Do not start indexing unless it is enabled in system configurations
        if (!Boolean.valueOf(sysConfigEJB.getValue(SysConfigKey.ENABLE_SEARCH_AND_INDEXING, "true"))) {
            log.info("Indexing not started since it is disabled");
            return;
        }

        try {
            InitialContext ic = new InitialContext();
            cache = (Cache) ic.lookup("java:jboss/infinispan/cache/ci20/session-cache");
        } catch (NamingException e) {
            log.error("Could not connect to infinispan cache; ", e);
        }

        Boolean indexingStarted = cache.get(INDEXING_STARTED_CHECK);

        if (indexingStarted == null || indexingStarted == Boolean.FALSE) {
            cache.put(INDEXING_STARTED_CHECK, Boolean.TRUE);
            buildIndex();
            super.start();
            return;
        }

        log.info("Lucene Indexing already started, returning..");
    }

    @Override
    public void fire() {
        start();
    }

    @Override
    protected void initTimer() {
        TimerConfig timerConfig = new TimerConfig("Lucene Indexer Timer", false);
        timer = timerService.createSingleActionTimer(TIMER_TIMEOUT_DEFAULT, timerConfig);
    }

    @PreDestroy
    @Override
    protected void destroy() {
        super.destroy();
    }

    @Timeout
    @Override
    public void task() {
        checkWorkers();
    }

    public boolean checkWorkers() {
        for (Future<Long> worker : workers) {
            if (!worker.isDone()) {
                initTimer();
                return false;
            }
        }
        long totalCount = countTotal();
        workers.clear();
        log.info("**** Indexing {} entities done in {}ms ****", totalCount, System.currentTimeMillis() - indexingStartTime);
        return true;
    }

    public void buildIndex() {
        log.info("**** Start indexing ****");
        indexingStartTime = System.currentTimeMillis();
        workers = startWorkers();
    }

    private List<Future<Long>> startWorkers() {
        long startTime = System.currentTimeMillis();
        log.info("*** Starting indexing workers... ***");
        Set<Class<?>> indexedClasses = SearchUtil.getIndexedClasses();
        for (Class c : indexedClasses) {
            if (!BaseEntity.class.isAssignableFrom(c)) {
                continue;
            }
            workers.add(indexWorkerEJB.doIndex(c));
        }
        log.info("*** Started {} workers in {}ms", workers.size(), System.currentTimeMillis() - startTime);
        return workers;
    }

    private long countTotal() {
        long total = 0;
        for (Future<Long> worker : workers) {
            try {
                Long count = worker.get();
                if (count != null) {
                    total += count.longValue();
                }
            } catch (InterruptedException ex) {
                log.warn("Could not get result from indexer worker! Worker was interrupted!");
            } catch (ExecutionException ex) {
                log.warn("Could not get result from indexer worker! There has been exception in execution!");
            }
        }
        return total;
    }
}
