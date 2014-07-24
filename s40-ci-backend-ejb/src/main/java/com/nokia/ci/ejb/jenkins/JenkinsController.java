/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.jenkins;

import com.nokia.ci.ejb.cicontroller.CIController;
import com.nokia.ci.ejb.cicontroller.CIControllerException;
import com.nokia.ci.ejb.model.BuildGroupCIServer;
import com.nokia.ci.ejb.model.CIServer;
import java.util.Map;

/**
 *
 * @author jajuutin
 */
public class JenkinsController extends CIController {

    private JenkinsClient jenkinsClient;

    public JenkinsController(BuildGroupCIServer ciServer) {
        super(ciServer);
        jenkinsClient = new JenkinsClient(ciServer.getUrl(),
                ciServer.getPort());

        if (ciServer.getUsername() != null && ciServer.getPassword() != null) {
            jenkinsClient.setupAuth(ciServer.getUsername(),
                    ciServer.getPassword());
        }
    }

    @Override
    public void build(String job) throws CIControllerException {
        buildOnJenkins(job, null);
    }

    @Override
    public void build(String job, Map<String, String> parameters)
            throws CIControllerException {

        buildOnJenkins(job, parameters);
    }

    private void buildOnJenkins(String job, Map<String, String> parameters)
            throws CIControllerException {
        if (job == null) {
            throw new CIControllerException("Null job as parameter");
        }
        
        try {
            getJenkinsClient().build(job, parameters);
        } catch (JenkinsClientException ex) {
            throw new CIControllerException(ex);
        }
    }

    public JenkinsClient getJenkinsClient() {
        return jenkinsClient;
    }

    public void setJenkinsClient(JenkinsClient jenkinsClient) {
        this.jenkinsClient = jenkinsClient;
    }
}