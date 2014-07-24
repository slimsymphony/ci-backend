/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.client.rest;

import com.nokia.ci.client.model.ProductView;
import com.nokia.ci.client.model.ProjectView;
import com.nokia.ci.client.model.VerificationView;
import javax.annotation.security.DenyAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Interface for accessing project resource.
 * @author vrouvine
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("projects")
@DenyAll
public interface ProjectResource {

    /**
     * Get all projects.
     * @return HTTP response with List of branches
     */
    @GET
    Response getProjects();

    /**
     * Get project by id.
     * If project does not exist, HTTP status code 404 is returned.
     * @param id Project id
     * @return HTTP response with {@link ProjectView} object
     */
    @GET
    @Path("/{id}")
    Response getProject(@PathParam(value = "id") Long id);

    /**
     * Get products for project.
     * If project does not exist, HTTP status code 404 is returned.
     * @param id Project id
     * @return HTTP response with list of {@link ProductView} objects.
     */
    @GET
    @Path("/{id}/products")
    Response getProducts(@PathParam(value = "id") Long id);
    
    /**
     * Get verifications for project.
     * If project does not exist, HTTP status code 404 is returned.
     * @param id Project id
     * @return HTTP response with list of {@link VerificationView} objects.
     */
    @GET
    @Path("/{id}/verifications")
    Response getVerifications(@PathParam(value = "id") Long id);
    
    /**
     * Get verification configurations for project.
     * If project does not exist, HTTP status code 404 is returned.
     * @param id Project id
     * @return HTTP response with list of {@link ProjectVerificationConfView} objects.
     */
    @GET
    @Path("/{id}/verificationConfs")
    Response getVerificationConfs(@PathParam(value = "id") Long id);
}
