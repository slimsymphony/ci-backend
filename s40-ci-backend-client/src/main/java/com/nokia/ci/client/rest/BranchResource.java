package com.nokia.ci.client.rest;

import com.nokia.ci.client.model.BranchView;
import java.net.URISyntaxException;
import javax.annotation.security.DenyAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Interface for accessing branch resources.
 *
 * @author vrouvine
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("branches")
@DenyAll
public interface BranchResource {

    /**
     * Creates new branch.
     *
     * @param branch Branch model
     * @return HTTP response with location header.
     */
    @POST
    Response createBranch(BranchView branch) throws URISyntaxException;

    /**
     * Get branch by id. If branch does not exist, HTTP status code 404 is
     * returned.
     *
     * @param id Branch id
     * @return HTTP response with {@link BranchView} object
     */
    @GET
    @Path(value = "/{id}")
    Response getBranch(@PathParam(value = "id") Long id);

    /**
     * Get all branches.
     *
     * @return HTTP response with List of branches
     */
    @GET
    Response getBranches();

    /**
     * Updates existing branch. If branch does not exist, HTTP status code 404
     * is returned.
     *
     * @param id Id of updated branch
     * @param branch {@link BranchView} object with updated information
     * @return HTTP response with updated {@link BranchView} object
     */
    @PUT
    @Path(value = "/{id}")
    Response updateBranch(@PathParam(value = "id") Long id, BranchView branch);
}
