package com.nokia.ci.api.resource;

import com.nokia.ci.api.session.SessionManager;
import com.nokia.ci.client.model.IncidentView;
import com.nokia.ci.client.rest.IncidentResource;
import com.nokia.ci.client.rest.JobResource;
import com.nokia.ci.ejb.IncidentEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Incident;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 4/8/13
 * Time: 10:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class IncidentResourceImpl implements IncidentResource {

    private static Logger log = LoggerFactory.getLogger(IncidentResourceImpl.class);

    @Inject
    IncidentEJB incidentEJB;

    @Override
    public Response createIncident(IncidentView incidentView) {
        log.info("Creating new Incident with description '{}'", incidentView.getDescription());
        Incident incident = incidentView.transformTo(Incident.class);
        incidentEJB.create(incident);
        URI uri = UriBuilder.fromResource(IncidentResource.class).path(incident.getId().toString()).build();
        log.debug("New Incident created to uri: {}", uri.toString());
        return Response.created(uri).build();
    }

    @Override
    public Response getIncident(@PathParam(value = "id") Long id) {
        log.info("Requesting incident by id {}", id);
        Incident incident = null;
        try {
            incident = incidentEJB.read(id);
        } catch (NotFoundException ex) {
            log.debug("Incident not found", ex);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        IncidentView view = viewFromEntity(incident);
        return Response.ok(view).build();
    }

    private IncidentView viewFromEntity(Incident incident) {
        IncidentView incidentView = new IncidentView();
        incidentView.setId(incident.getId());
        incidentView.setTime(incident.getTime());
        incidentView.setType(incident.getType());
        incidentView.setDescription(incident.getDescription());
        incidentView.setCheckTime(incident.getCheckTime());
        incidentView.setCheckUser(incident.getCheckUser());
        return incidentView;
    }


}
