/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.client.rest;

import com.nokia.ci.client.model.AuthRequestView;
import javax.annotation.security.DenyAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author jajuutin
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("auth")
@DenyAll
public interface AuthResource {
    /**
     * Login.
     * @param loginView
     * @return 
     */
    @POST
    @Path(value = "/login")
    Response login(AuthRequestView authRequestView);
    
    /**
     * Logout.
     * @return
     */
    @GET
    @Path(value = "/logout/{token}")
    Response logout(@PathParam(value = "token") String token);
}
