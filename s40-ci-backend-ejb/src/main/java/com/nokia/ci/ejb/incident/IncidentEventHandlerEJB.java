package com.nokia.ci.ejb.incident;

import com.nokia.ci.ejb.IncidentEJB;
import com.nokia.ci.ejb.event.IncidentEventContent;
import com.nokia.ci.ejb.model.Incident;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 4/5/13
 * Time: 2:42 PM
 * To change this template use File | Settings | File Templates.
 */
@LocalBean
@Stateless
public class IncidentEventHandlerEJB {
    @EJB
    private IncidentEJB incidentEJB;

    @Asynchronous
    public void persist(@Observes(during = TransactionPhase.AFTER_COMPLETION) IncidentEventContent incidentEvent) {
        Incident incident = new Incident();
        incident.setTime(new Date());
        incident.setDescription(incidentEvent.getDescription());
        incident.setType(incidentEvent.getType());
        incidentEJB.create(incident);
    }


}
