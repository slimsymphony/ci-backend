/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.reaper;

import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.hasingleton.AbstractHASingletonTimer;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ejb.util.LDAPUser;
import com.nokia.ci.ejb.util.LDAPUtil;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import org.infinispan.Cache;
import org.quartz.CronExpression;

/**
 *
 * @author hhellgre
 */
@Singleton
public class SysUserReaper extends AbstractHASingletonTimer {

    @EJB
    private SysConfigEJB sysConfigEJB;
    @EJB
    private SysUserEJB sysUserEJB;
    @Resource
    TimerService timerService;
    @PersistenceContext(unitName = "NokiaCI-PU")
    EntityManager em;
    private Cache<String, Date> cache;
    private static final String SYSUSER_REAPER_DONE = "SysUserReaperDone";
    private static final long TIMER_TIMEOUT_DEFAULT = 60000;
    private static final String USER_REAPER_CRON_DEFAULT = "0 0 0 * * ?";
    private String cronValue;

    @Override
    public void start() {
        super.start();
    }

    @Override
    protected void initTimer() {
        TimerConfig timerConfig = new TimerConfig("SysUser reaper timer timeout", false);
        timer = timerService.createSingleActionTimer(TIMER_TIMEOUT_DEFAULT, timerConfig);
    }

    @PreDestroy
    @Override
    protected void destroy() {
        super.destroy();
    }

    @Override
    public void fire() {
        // Move last run to January 1, 1970
        cache.put(SYSUSER_REAPER_DONE, new Date(0));
        super.fire();
    }

    @Override
    @Timeout
    public void task() {
        cronValue = sysConfigEJB.getValueNoLog(SysConfigKey.SYSUSER_REAPER_CRON, USER_REAPER_CRON_DEFAULT);
        try {
            InitialContext ic = new InitialContext();
            cache = (Cache) ic.lookup("java:jboss/infinispan/cache/ci20/session-cache");
        } catch (NamingException e) {
            log.error("Could not connect to infinispan cache; ", e);
        }

        try {
            if (!cache.containsKey(SYSUSER_REAPER_DONE)) {
                cache.put(SYSUSER_REAPER_DONE, new Date());
                return;
            }

            cronValue = sysConfigEJB.getValueNoLog(SysConfigKey.SYSUSER_REAPER_CRON, USER_REAPER_CRON_DEFAULT);
            CronExpression cron = new CronExpression(cronValue);
            Date lastRun = cache.get(SYSUSER_REAPER_DONE);

            Date nextRun = null;
            if (lastRun != null) {
                nextRun = cron.getNextValidTimeAfter(lastRun);
            }

            if (nextRun != null && nextRun.after(new Date())) {
                return;
            }

            reapUsers();
            cache.put(SYSUSER_REAPER_DONE, new Date());
        } catch (ParseException ex) {
            log.warn("*********** SysUser reaper cron expression is invalid {}, skipping reaping ***********", cronValue);
        } finally {
            initTimer();
        }
    }

    private void reapUsers() {
        long startTime = System.currentTimeMillis();
        log.info("*********** SysUser reaper starting ***********");

        List<SysUser> users = new ArrayList<SysUser>();
        users.addAll(sysUserEJB.readAll());
        LDAPUtil util = new LDAPUtil();
        for (SysUser u : users) {
            try {
                LDAPUser ldapUser = util.getByUsername(u.getLoginName());
                if (ldapUser == null) {
                    log.info("User {} could not be found from LDAP, removing from database", u);
                    sysUserEJB.delete(u);
                }
            } catch (Exception e) {
                log.error("Error occured when trying to find user " + u.getLoginName() + " from LDAP: ", e);
            }
        }

        log.info("*********** SysUser reaping done in {} ms ***********", System.currentTimeMillis() - startTime);
    }
}
