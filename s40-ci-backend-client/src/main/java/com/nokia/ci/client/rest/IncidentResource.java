package com.nokia.ci.client.rest;

import com.nokia.ci.client.model.IncidentView;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 4/8/13 Time: 10:49 AM To change
 * this template use File | Settings | File Templates.
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("incidents")
public interface IncidentResource {

    @POST
    Response createIncident(IncidentView incidentView);

    @GET
    @Path(value = "/{id}")
    Response getIncident(@PathParam(value = "id") Long id);
}
