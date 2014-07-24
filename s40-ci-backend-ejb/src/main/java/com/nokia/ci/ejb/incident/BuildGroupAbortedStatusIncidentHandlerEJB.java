package com.nokia.ci.ejb.incident;

import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.event.BuildGroupFinishedEvent;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.model.IncidentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 3/22/13 Time: 9:47 AM To change
 * this template use File | Settings | File Templates.
 */
@Stateless
@LocalBean
public class BuildGroupAbortedStatusIncidentHandlerEJB extends BaseIncidentHandler {

    private static Logger log = LoggerFactory.getLogger(BuildGroupAbortedStatusIncidentHandlerEJB.class);
    private final String DESC = "Overall status of verification '%s'(BuildGroupId : %s) was marked to ABORTED.";
    @EJB
    private BuildGroupEJB buildGroupEJB;

    @Asynchronous
    public void buildGroupFinished(@Observes(during = TransactionPhase.AFTER_SUCCESS) @BuildGroupFinishedEvent Long id) {
        log.info("Handling build group aborted status for buildGroup id : {}", id);
        try {
            BuildGroup buildGroup = buildGroupEJB.read(id);
            if (buildGroup.getStatus().equals(BuildStatus.ABORTED)) {
                String description = String.format(DESC, buildGroup.getJobDisplayName(), buildGroup.getId());
                createIncident(IncidentType.DELIVERY_CHAIN, description);
            }
        } catch (NotFoundException e) {
            log.error("BuildGroup id : {} not found", id);
        }
    }
}
