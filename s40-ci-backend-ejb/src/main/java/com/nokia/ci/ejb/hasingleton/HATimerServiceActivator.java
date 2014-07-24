package com.nokia.ci.ejb.hasingleton;

import org.jboss.as.clustering.singleton.SingletonService;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.ServerEnvironmentService;
import org.jboss.msc.service.DelegatingServiceContainer;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service activator that installs the HATimerService as a clustered singleton
 * service during deployment.
 *
 * @author vrouvine
 */
public class HATimerServiceActivator implements ServiceActivator {

    private final Logger log = LoggerFactory.getLogger(HATimerServiceActivator.class);

    @Override
    public void activate(ServiceActivatorContext context) {
        log.info("****** Installing HA Singleton Timer Service ******");

        HATimerService service = new HATimerService();
        SingletonService<String> singleton = new SingletonService<String>(service, HATimerService.SINGLETON_SERVICE_NAME);
        /*
         * We can pass a chain of election policies to the singleton, for example to tell JGroups to prefer running the singleton on a node with a
         * particular name
         */
        ServiceContainer serviceContainer = new DelegatingServiceContainer(context.getServiceTarget(), context.getServiceRegistry());
        ServiceController<String> serviceController = singleton.build(serviceContainer)
                .addDependency(ServerEnvironmentService.SERVICE_NAME, ServerEnvironment.class, service.getEnv())
                .install();
        serviceController.setMode(ServiceController.Mode.ACTIVE);
        log.info("****** HA Singleton Timer Service installed ******");
    }
}