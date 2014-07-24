package com.nokia.ci.ejb.incident;

import com.nokia.ci.ejb.SlavePoolEJB;
import com.nokia.ci.ejb.SlaveStatEJB;
import com.nokia.ci.ejb.event.LoadBalancerStatEvent;
import com.nokia.ci.ejb.model.IncidentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import java.util.Date;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author larryang
 */
@Stateless
@LocalBean
public class LoadBalancerStatIncidentHandlerEJB extends BaseIncidentHandler {

    private static Logger log = LoggerFactory.getLogger(LoadBalancerStatIncidentHandlerEJB.class);

    @EJB
    private SlaveStatEJB slaveStatEJB;
    @EJB
    private SlavePoolEJB slavePoolEJB;

    @Asynchronous
    public void collectLoadBalancerStat(@Observes(during = TransactionPhase.AFTER_SUCCESS) @LoadBalancerStatEvent Long id) {
        log.info("Handling load balancer statistical data collection : {}", id);
        try {
            String desc = slaveStatEJB.generateSlaveStat(slavePoolEJB.read(id).getName(), new Date());

            if (StringUtils.isNotEmpty(desc)) {
                createIncident(IncidentType.LOAD_BALANCER, desc);
            }

        } catch (Exception e) {
            log.error("Exception when collecting load balancer statistical data for slave pool {}. Detailed reasons: {}.", id, e.getMessage() + e.getStackTrace());
        }
    }
}
