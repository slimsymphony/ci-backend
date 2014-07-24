package com.nokia.ci.client.rest;

import com.nokia.ci.ejb.model.Build;
import javax.annotation.security.DenyAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Interface for accessing file resource.
 *
 * @author larryang
 */
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
@Path("files")
public interface FileResource {

    /**
     * Get file by uuid.
     *
     * @param uuid UserFile uuid
     * @return {@link Build} object
     */
    @GET
    @Path("/{uuid}")
    Response getRestFile(@PathParam(value = "uuid") String uuid);

    @GET
    @Path("/system/{uuid}")
    Response getSystemFile(@PathParam(value = "uuid") String uuid);
}
