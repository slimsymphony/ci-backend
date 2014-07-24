package com.nokia.ci.ejb.hasingleton;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.naming.Binding;

import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.jboss.as.server.ServerEnvironment;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A service to start all {@link HASingletonTimer} instances in a clustered
 * environment. The service will ensure that the timers are initialized only
 * once in a cluster.
 *
 * @author vrouvine
 */
public class HATimerService implements Service<String> {

    private static final Logger log = LoggerFactory.getLogger(HATimerService.class);
    public static final ServiceName SINGLETON_SERVICE_NAME = ServiceName.JBOSS.append("ci20backend", "ha", "singleton", "timer");
    /**
     * A flag whether the service is started.
     */
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final InjectedValue<ServerEnvironment> env = new InjectedValue<ServerEnvironment>();
    private String nodeName;

    /**
     * @return the name of the server node
     */
    @Override
    public String getValue() throws IllegalStateException, IllegalArgumentException {
        if (!started.get()) {
            throw new IllegalStateException("The service '" + this.getClass().getName() + "' is not ready!");
        }
        return this.nodeName;
    }

    @Override
    public void start(StartContext arg0) throws StartException {
        log.info("***** Starting {} *****", this.getClass().getName());
        if (!started.compareAndSet(false, true)) {
            throw new StartException("The service is already started!");
        }

        this.nodeName = this.env.getValue().getNodeName();
        startTimers();
        log.info("***** {} started ******", this.getClass().getName());
    }

    @Override
    public void stop(StopContext arg0) {
        log.info("***** Stopping {} *****", this.getClass().getName());
        if (!started.compareAndSet(true, false)) {
            log.warn("The service {} is not active!", this.getClass().getName());
            return;
        }
        log.info("***** {} stopped ******", this.getClass().getName());
    }

    public InjectedValue<ServerEnvironment> getEnv() {
        return env;
    }

    /**
     * Starts all {@link HASingletonTimer} instances.
     *
     * @throws StartException If starting of {@link HASingletonTimer} instance
     * fails.
     */
    private void startTimers() throws StartException {
        try {
            InitialContext ic = new InitialContext();
            Set<HASingletonTimer> startedTimers = new HashSet<HASingletonTimer>();
            NamingEnumeration<Binding> ejbBindings = ic.listBindings("java:global/s40-ci-backend-ear/s40-ci-backend-ejb");
            while (ejbBindings.hasMore()) {
                Binding ejbBinding = ejbBindings.next();
                if (!(ejbBinding.getObject() instanceof HASingletonTimer)) {
                    continue;
                }
                HASingletonTimer timer = (HASingletonTimer) ejbBinding.getObject();
                if (startedTimers.contains(timer)) {
                    continue;
                }
                timer.start();
                startedTimers.add(timer);
            }
        } catch (NamingException e) {
            throw new StartException("Starting timers from context failed!", e);
        }
    }
}