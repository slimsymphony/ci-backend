package com.nokia.ci.ejb.loadbalancer;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

import com.nokia.ci.ejb.SlaveInstanceEJB;
import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.hasingleton.AbstractHASingletonTimer;
import com.nokia.ci.ejb.model.SysConfigKey;
import javax.annotation.PreDestroy;

@Singleton
public class DetachReservedSlaveTimer extends AbstractHASingletonTimer {

    private static final long DETACH_RESERVED_SLAVE_TIMER_INTERVAL_DEFAULT = 30 * 60 * 1000;
    @Resource
    TimerService timerService;
    @EJB
    private SysConfigEJB sysConfigEJB;
    @EJB
    private SlaveInstanceEJB slaveInstanceEJB;
    private long check_interval = DETACH_RESERVED_SLAVE_TIMER_INTERVAL_DEFAULT;

    @Override
    @Timeout
    protected void task() {
        log.info("**** DetachReservedSlaveTimer task triggered ****");
        long startTime = System.currentTimeMillis();
        try {
            slaveInstanceEJB.detachExpiredSlaves();
        } catch (Exception ex) {
            log.error("Error in DetachReservedSlaveTimer!", ex);
        } finally {
            initTimer();
        }
        log.info("**** DetachReservedSlaveTimer task done in {}ms ****", System.currentTimeMillis() - startTime);
    }

    @PreDestroy
    @Override
    protected void destroy() {
        super.destroy();
    }

    @Override
    protected void initTimer() {
        // Try to retrieve check interval from sysconfig
        check_interval = sysConfigEJB.getValue(SysConfigKey.DETACH_RESERVED_SLAVE_TIMER_INTERVAL,
                DETACH_RESERVED_SLAVE_TIMER_INTERVAL_DEFAULT);
        // Set up timer
        TimerConfig timerConfig = new TimerConfig("DetachReservedSlaveTimer Timer", false);
        log.info("Creating single action timer with check interval {}", check_interval);
        timer = timerService.createSingleActionTimer(check_interval, timerConfig);
    }
}
