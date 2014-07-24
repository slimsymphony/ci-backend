package com.nokia.ci.ejb.hasingleton;

import javax.ejb.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract super class for HASingleton timers.
 *
 * @author vrouvine
 */
public abstract class AbstractHASingletonTimer implements HASingletonTimer {

    protected Logger log = LoggerFactory.getLogger(this.getClass());
    protected Timer timer;

    @Override
    public void start() {
        log.info("Starting timer.");
        initTimer();
    }

    protected void destroy() {
        log.info("Stopping timer.");
        if (timer != null) {
            timer.cancel();
        }
    }

    abstract protected void initTimer();

    abstract protected void task();

    public void fire() {
        if (timer != null) {
            timer.cancel();
        }
        task();
    }
}
