/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.index;

import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.hasingleton.AbstractHASingletonTimer;
import com.nokia.ci.ejb.model.SysConfigKey;
import java.text.ParseException;
import java.util.Date;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.infinispan.Cache;
import org.quartz.CronExpression;

/**
 *
 * @author hhellgre
 */
@Singleton
public class SearchIndexOptimizator extends AbstractHASingletonTimer {

    @EJB
    private SysConfigEJB sysConfigEJB;
    @Resource
    TimerService timerService;
    @PersistenceContext(unitName = "NokiaCI-PU")
    EntityManager em;
    private Cache<String, Date> cache;
    private static final String INDEXING_OPTIMIZATION_DONE = "LuceneIndexingOptimizationDone";
    private static final long TIMER_TIMEOUT_DEFAULT = 1000;
    private static final String INDEX_OPTIMIZATION_CRON_DEFAULT = "0 0 23 * * ?";
    private String cronValue;

    @Override
    public void start() {
        // Do not start index optimization unless it is enabled in system configurations
        if (!Boolean.valueOf(sysConfigEJB.getValue(SysConfigKey.ENABLE_SEARCH_AND_INDEXING, "true"))) {
            log.info("Index optimization not started since it is disabled");
            return;
        }

        try {
            InitialContext ic = new InitialContext();
            cache = (Cache) ic.lookup("java:jboss/infinispan/cache/ci20/session-cache");
        } catch (NamingException e) {
            log.error("Could not connect to infinispan cache; ", e);
        }

        super.start();
    }

    @Override
    public void fire() {
        // Move last run to January 1, 1970
        cache.put(INDEXING_OPTIMIZATION_DONE, new Date(0));
        super.fire();
    }

    @Override
    protected void initTimer() {
        TimerConfig timerConfig = new TimerConfig("Lucene IndexeOptimizator Timer", false);
        timer = timerService.createSingleActionTimer(TIMER_TIMEOUT_DEFAULT, timerConfig);
    }

    @PreDestroy
    @Override
    protected void destroy() {
        super.destroy();
    }

    @Override
    @Timeout
    public void task() {
        try {
            if (!cache.containsKey(INDEXING_OPTIMIZATION_DONE)) {
                cache.put(INDEXING_OPTIMIZATION_DONE, new Date());
                return;
            }

            cronValue = sysConfigEJB.getValueNoLog(SysConfigKey.INDEX_OPTIMIZATION_CRON, INDEX_OPTIMIZATION_CRON_DEFAULT);
            CronExpression cron = new CronExpression(cronValue);
            Date lastRun = cache.get(INDEXING_OPTIMIZATION_DONE);

            Date nextRun = cron.getNextValidTimeAfter(lastRun);

            if (nextRun != null && nextRun.after(new Date())) {
                return;
            }

            optimizeIndex();
            cache.put(INDEXING_OPTIMIZATION_DONE, new Date());
        } catch (ParseException ex) {
            log.warn("*********** Index optimization cron expression is invalid {}, skipping optimization ***********", cronValue);
        } finally {
            initTimer();
        }
    }

    private void optimizeIndex() {
        long startTime = System.currentTimeMillis();
        log.info("*********** Search index optimization starts ***********");
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);
        SearchFactory factory = fullTextEntityManager.getSearchFactory();
        factory.optimize();
        log.info("*********** Search index optimization done in {} ms ***********", System.currentTimeMillis() - startTime);
    }
}
