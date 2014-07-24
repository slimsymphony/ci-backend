package com.nokia.ci.ejb.gerrit;

import com.nokia.ci.ejb.GerritEJB;
import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.hasingleton.AbstractHASingletonTimer;
import com.nokia.ci.ejb.model.Gerrit;
import com.nokia.ci.ejb.model.SysConfigKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.annotation.PreDestroy;
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
public class GerritListenerManager extends AbstractHASingletonTimer {

    private static final long TIMER_TIMEOUT_DEFAULT = 60 * 1000;
    @Resource
    TimerService timerService;
    @EJB
    private SysConfigEJB sysConfigEJB;
    @EJB
    private GerritEJB gerritEJB;
    @EJB
    private GerritListener listener;
    private Map<Gerrit, Future<Boolean>> gerritsListened = new HashMap<Gerrit, Future<Boolean>>();

    @Override
    protected void initTimer() {
        long timeout = sysConfigEJB.getValue(SysConfigKey.GERRIT_LISTENER_TIMER_TIMEOUT, TIMER_TIMEOUT_DEFAULT);
        TimerConfig timerConfig = new TimerConfig("Gerrit Listener Manager Timer", false);
        log.info("Creating single action timer with timeout {}", timeout);
        timer = timerService.createSingleActionTimer(timeout, timerConfig);
    }

    @PreDestroy
    @Override
    protected void destroy() {
        super.destroy();
    }

    @Override
    @Timeout
    protected void task() {
        log.info("**** Gerrit Listener Manager timer task triggered ****");
        long startTime = System.currentTimeMillis();

        List<Gerrit> gerrits = new ArrayList<Gerrit>();
        gerrits.addAll(gerritEJB.readAll());

        // Check for closed connections and remove from map
        Map<Gerrit, Future<Boolean>> listened = new HashMap<Gerrit, Future<Boolean>>();
        listened.putAll(gerritsListened);

        for (Map.Entry<Gerrit, Future<Boolean>> e : listened.entrySet()) {
            if (e.getValue().isDone()) {
                try {
                    Boolean ret = e.getValue().get();
                    log.info("Gerrit listener for gerrit {} has stopped with value {}", e.getKey(), ret);

                    if (ret == false) {
                        /* TODO: The listener stopped with error, we might want to manually
                         *       check the gerrit for open commits.. Though we need an implementation
                         *       for this
                         */
                        log.warn("Gerrit listener for gerrit {} returned with FAILURE", e.getKey());
                    }

                } catch (Exception ex) {
                    log.warn("Failed to get exit value of listener for gerrit {}", e.getKey());
                }
                gerritsListened.remove(e.getKey());
            }
        }

        // Check for new gerrits and create handlers
        for (Gerrit g : gerrits) {
            if (g.getListenStream() == null || g.getListenStream() == false) {
                continue;
            }

            if (!gerritsListened.containsKey(g)) {
                createGerritListener(g);
            }
        }

        log.info("**** Gerrit Listener Manager timer task done in {}ms ****", System.currentTimeMillis() - startTime);
        initTimer();
    }

    private void createGerritListener(Gerrit gerrit) {
        log.info("Creating and starting new gerrit listener for {}", gerrit);
        gerritsListened.put(gerrit, listener.listen(gerrit.getId()));
    }
}
