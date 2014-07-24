/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.client.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * 
 * @author jajuutin
 */
@Consumes(MediaType.MEDIA_TYPE_WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
@Path("jobnotification")
public interface NotificationPluginResource {
    /**
     * Receive job notification.
     * @return HTML response.
     */
    @POST
    public Response processNotification(byte[] data);
    
    /**
     * Receive creator notification.
     * @return HTML response.
     */
    @POST
    @Path(value="/creator")
    public Response processCreatorNotification(byte[] data);    
    
    /**
     * Receive gerrit trigger job notifications.
     * @return HTML response.
     */
    @POST
    @Path(value="/gerrittrigger")
    public Response processGerritTriggerNotification(byte[] data);        
}
