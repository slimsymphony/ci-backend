/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.client.rest;

import com.nokia.ci.client.model.BranchView;
import com.nokia.ci.client.model.JobVerificationConfView;
import com.nokia.ci.client.model.JobView;
import com.nokia.ci.ejb.model.Job;
import java.net.URISyntaxException;
import java.util.List;
import javax.annotation.security.DenyAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Interface for accessing job resource.
 *
 * @author vrouvine
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("jobs")
@DenyAll
public interface JobResource {

    /**
     * Creates new job.
     *
     * @param job Job model
     * @return HTTP response with location header.
     */
    @POST
    Response createJob(JobView job,
            @DefaultValue("0") @QueryParam("projectId") Long projectId,
            @DefaultValue("") @HeaderParam(Header.AUTH_TOKEN) String token)
            throws URISyntaxException;

    /**
     * Get job by id.
     *
     * @param id Job id
     * @return {@link Job} object
     */
    @GET
    @Path(value = "/{id}")
    Response getJob(@PathParam(value = "id") Long id);

    /**
     * Get jobs.
     * Fetches jobs by token.
     * 
     * @param token Authentication token as Header parameter
     * @return List of jobs
     */
    @GET
    Response getJobs(@DefaultValue("") @HeaderParam(Header.AUTH_TOKEN) String token);

    /**
     * Updates existing job. If job does not exist, HTTP status code 404 is
     * returned.
     *
     * @param id Id of updated job
     * @param job {@link JobView} object with updated information
     * @return HTTP response with updated {@link BranchView} object
     */
    @PUT
    @Path(value = "/{id}")
    Response updateJob(@PathParam(value = "id") Long id, JobView job);

    /**
     * Delete job
     *
     * @param id Id of deleted job
     * @return HTTP response
     */
    @DELETE
    @Path(value = "/{id}")
    Response deleteJob(@PathParam(value = "id") Long id,
            @DefaultValue("") @HeaderParam(Header.AUTH_TOKEN) String token);

    /**
     * Start a job with verification conf.
     *
     * @param id
     * @param refSpec
     * @return
     */
    @GET
    @Path(value = "/{id}/start")
    Response start(@PathParam(value = "id") Long id,
            @DefaultValue("") @QueryParam("refSpec") String refSpec);

    /**
     * Get all verification configurations for job.
     *
     * If job does not exist, HTTP status code 404 is returned.
     *
     * @param id Job id
     * @return HTTP response with list of {@link JobVerificationConfView}
     * objects.
     */
    @GET
    @Path(value = "/{id}/verificationConfs")
    Response getVerificationConfs(@PathParam(value = "id") Long id);

    /**
     * Saves given verification configurations for job.
     *
     * @param id Job id
     * @param confs List of job verification configurations
     * @return HTTP response code 200
     */
    @PUT
    @Path(value = "/{id}/verificationConfs")
    Response saveVerificationConfs(@PathParam(value = "id") Long id,
            List<JobVerificationConfView> confs);
}
