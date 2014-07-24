package com.nokia.ci.ejb.incident;

import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.event.BuildGroupFinishedEvent;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.model.IncidentType;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.StatusTriggerPattern;
import com.nokia.ci.ejb.util.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ejb.Asynchronous;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 3/8/13 Time: 2:00 PM To change
 * this template use File | Settings | File Templates.
 */
@Stateless
@LocalBean
public class LoadBalancerIncidentHandlerEJB extends BaseIncidentHandler {

    private static Logger log = LoggerFactory.getLogger(LoadBalancerIncidentHandlerEJB.class);
    private String DESC = "Limit for reserved slave instances of pool '%s' for master '%s' exceeded";

    public void maxSlaveInstancesForMasterExceeded(String master, String slavePool) {
        createLogEntryAndSave(master, slavePool);
    }

    private void createLogEntryAndSave(String master, String slavePool) {
        String description = String.format(DESC, slavePool, master);
        createIncident(IncidentType.LOAD_BALANCER, description);
        log.info("Log entry has been created for master {}", master);
    }
 
}
