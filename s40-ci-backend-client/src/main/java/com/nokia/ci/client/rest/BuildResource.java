/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.client.rest;

import com.nokia.ci.client.model.BuildEventView;
import com.nokia.ci.client.model.BuildFailureView;
import com.nokia.ci.client.model.BuildView;
import com.nokia.ci.client.model.MemConsumptionView;
import com.nokia.ci.client.model.MetricsDatasetView;
import com.nokia.ci.client.model.TestCaseStatView;
import com.nokia.ci.client.model.TestCoverageView;
import com.nokia.ci.ejb.model.Build;
import java.net.URISyntaxException;
import java.util.List;
import javax.annotation.security.DenyAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author vrouvine
 */
@Produces(value = MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
@Path("builds")
public interface BuildResource {

    /**
     * Creates new build.
     *
     * @param build Build model
     * @return HTTP response with location header.
     */

    @DenyAll
    @POST
    Response createBuild(BuildView build) throws URISyntaxException;
    
    /**
     * Get build by id.
     *
     * @param id Build id
     * @return {@link Build} object
     */
    @DenyAll
    @GET
    @Path("/{id}")
    Response getBuild(@PathParam(value = "id") Long id);
   
    /**
     * Get all builds with refSpec query parameter.
     * If refSpec query parameter is empty HTTP Code 400 is returned.
     * If refSpec query parameter is given only builds with matching
     * refSpec are returned.
     *
     * @param refSpec given refSpec string
     * @return HTTP Code 200 and list of builds in body.
     */
    @DenyAll
    @GET            
    Response getBuilds(@DefaultValue(value = "") @QueryParam("refSpec") String refSpec);
    
    /**
     * Create build events.
     *
     * @param id Build id
     * @return Http response code "CREATED" if succesful.
     */
    @POST
    @Path("/{id}/buildEvents")
    Response createBuildEvents(@PathParam(value = "id") Long id, List<BuildEventView> buildEvents);
    
    /**
     * Sets executor property for build with given id.
     * 
     * @param id Build id.
     * @param executor string value of executor
     * @return HTTP Code 200 (OK) when executor set successfully.
     */
    @POST
    @Path("/{id}/executor")
    Response setExecutor(@PathParam(value = "id") Long id, String executor);
    
    /**
     * Create build related memory consumptions (one build in one post).
     *
     * @param id Build id
     * @return Http response code "CREATED" if succesful.
     */
    @POST
    @Path("/{id}/memConsumptions")
    Response createMemConsumptions(@PathParam(value = "id") Long id, List<MemConsumptionView> memConsumptions);
    
    /**
     * Create build related test case stats (one build in one post).
     *
     * @param id Build id
     * @return Http response code "CREATED" if succesful.
     */
    @POST
    @Path("/{id}/testCaseStats")
    Response createTestCaseStats(@PathParam(value = "id") Long id, List<TestCaseStatView> testCaseStats);
    
    /**
     * Create build related test coverages (one build in one post).
     *
     * @param id Build id
     * @return Http response code "CREATED" if succesful.
     */
    @POST
    @Path("/{id}/testCoverages")
    Response createTestCoverages(@PathParam(value = "id") Long id, List<TestCoverageView> testCoverages);
    
    /**
     * Create build related failures.
     *
     * @param id Build id
     * @return Http response code "CREATED" if succesful.
     */
    @POST
    @Path("/{id}/buildFailures")
    Response createBuildFailures(@PathParam(value = "id") Long id, List<BuildFailureView> buildFailures);
    
    /**
     * Create metrics related data for a build.
     *
     * @param none
     * @return Http response code "CREATED" if succesful.
     */    
    @POST
    @Path("/{id}/metricsData")
    Response createMetricsData(@PathParam(value = "id") Long id, MetricsDatasetView metricsDatasetView);
    
}
