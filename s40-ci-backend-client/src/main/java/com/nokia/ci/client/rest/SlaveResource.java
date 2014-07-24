package com.nokia.ci.client.rest;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author jaakkpaa
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/slaves")
public interface SlaveResource {

    /**
     * Apparently JBoss ignores the method names and decides which method to
     * call on request by the function parameters. *
     */
    @GET
    @Path("/{slavePool}")
    public Response action(@PathParam("slavePool") String slavePool,
                           @QueryParam("type") String type,
                           @QueryParam("labels") String slaveLabel,
                           @QueryParam("executors") Long executors,
                           @QueryParam("master") String master,
                           @QueryParam("release_id") String release_id);
}
