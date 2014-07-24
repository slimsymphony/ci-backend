package com.nokia.ci.ejb.reaper;

import com.nokia.ci.ejb.BuildEJB;
import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.exception.InvalidPhaseException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.hasingleton.AbstractHASingletonTimer;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildPhase;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.model.SysConfigKey;

import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

/**
 *
 * @author hhellgre
 */
@Singleton
public class BuildGroupReaper extends AbstractHASingletonTimer {

    private static final long TIMER_TIMEOUT_DEFAULT = 60 * 1000;
    private static final long DEFAULT_REAP_JOBSTART_TIMEOUT = 60 * 60 * 1000;         // 1 hour
    private static final long DEFAULT_REAP_STARTED_TIMEOUT = 2 * 24 * 60 * 60 * 1000; // 2 days
    @Resource
    TimerService timerService;
    @EJB
    private SysConfigEJB sysConfigEJB;
    @EJB
    private BuildGroupEJB buildGroupEJB;
    @EJB
    private BuildEJB buildEJB;
    private long timeout;
    private long jobStartReapTimeout;
    private long reapTimeout;

    @Override
    @Timeout
    protected void task() {
        log.info("**** BuildGroup reaper task triggered ****");
        long startTime = System.currentTimeMillis();
        runReaper();
        initTimer();
        log.info("**** BuildGroup reaper task done in {}ms ****", System.currentTimeMillis() - startTime);
    }

    public void runReaper() {
        List<BuildGroup> groups = buildGroupEJB.getStartedBuildGroups();

        for (BuildGroup g : groups) {
            Long lastUpdate = g.getStartTime().getTime();
            Boolean reapGroup = false;
            if (g.getModified() != null) {
                lastUpdate = g.getModified().getTime();
            }

            Long timeSinceStart = System.currentTimeMillis() - lastUpdate;

            if (timeSinceStart > jobStartReapTimeout) {
                log.warn("Buildgroup {} has been not been updated in {} ms, checking for reaping", g, timeSinceStart);
                // TODO: Problem logger / email action, something might be wrong with these builds
                reapGroup = true;
            }

            List<Build> builds = buildGroupEJB.getBuilds(g.getId());

            // TODO: what to do with long running single builds? Check status from jenkins or
            //       just turn them FAILED after some other time period (like 24 hours)?
            //       currently this only reaps groups that have started but no actual builds
            //       have finished or started
            for (Build b : builds) {
                if (b.getPhase() != BuildPhase.CONFIGURED) {
                    reapGroup = false;
                    break;
                }
            }

            // Ultimate reap timeout for started jobs
            if (timeSinceStart > reapTimeout) {
                reapGroup = true;
            }

            if (reapGroup == true) {
                // Reap this build group
                log.error("******* Reaping build group {}, start time {} ********", g, g.getStartTime());
                // TODO: Problem logger / email action, something is really wrong with these builds
                try {
                    for (Build b : builds) {
                        if (b.getPhase() != BuildPhase.FINISHED) {
                            b.setStatus(BuildStatus.ABORTED);
                            b.setPhase(BuildPhase.FINISHED);
                            b.setEndTime(new Date());
                            buildEJB.update(b);
                        }
                    }
                    buildGroupEJB.updateStatus(g.getId(), BuildStatus.ABORTED);
                } catch (InvalidPhaseException ex) {
                    log.debug("Invalid phase NOT_BUILD for builgroup {}", g);
                } catch (NotFoundException ex) {
                    log.debug("Could not find buildgroup {} or build", g);
                }
            }
        }
    }

    @Override
    protected void initTimer() {
        timeout = sysConfigEJB.getValue(SysConfigKey.BUILDGROUP_REAPER_TIMER_TIMEOUT, TIMER_TIMEOUT_DEFAULT);
        jobStartReapTimeout = sysConfigEJB.getValue(SysConfigKey.BUILDGROUP_REAPER_JOBSTART_TIMEOUT, DEFAULT_REAP_JOBSTART_TIMEOUT);
        reapTimeout = sysConfigEJB.getValue(SysConfigKey.BUILDGROUP_REAPER_TIMEOUT, DEFAULT_REAP_STARTED_TIMEOUT);
        TimerConfig timerConfig = new TimerConfig("BuildGroup reaper Timer", false);
        log.info("Creating single action timer with timeout {}", timeout);
        timer = timerService.createSingleActionTimer(timeout, timerConfig);
    }
}
