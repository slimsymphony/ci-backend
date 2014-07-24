/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.cicontroller;

import com.nokia.ci.ejb.model.BuildGroupCIServer;
import com.nokia.ci.ejb.model.CIServer;
import java.util.Map;

/**
 *
 * @author jajuutin
 */
public abstract class CIController {

    private BuildGroupCIServer server;

    protected CIController(BuildGroupCIServer server) {
        this.server = server;
    }

    public BuildGroupCIServer getServer() {
        return server;
    }

    public void setServer(BuildGroupCIServer server) {
        this.server = server;
    }

    public abstract void build(String job) throws CIControllerException;

    public abstract void build(String job, Map<String, String> parameters)
            throws CIControllerException;
}
