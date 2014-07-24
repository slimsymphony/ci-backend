/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.cicontroller.CIController;
import com.nokia.ci.ejb.cicontroller.CIControllerException;
import com.nokia.ci.ejb.cicontroller.CIParam;
import com.nokia.ci.ejb.exception.JobStartException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.jenkins.JenkinsController;
import com.nokia.ci.ejb.model.BuildGroupCIServer;
import com.nokia.ci.ejb.model.SysConfigKey;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ttyppo
 */
@Stateless
@LocalBean
public class BuildGroupCIServerEJB extends CrudFunctionality<BuildGroupCIServer> implements Serializable {

    private static Logger log = LoggerFactory.getLogger(BuildGroupCIServerEJB.class);

    @EJB
    SysConfigEJB sysConfigEJB;
    
    public BuildGroupCIServerEJB() {
        super(BuildGroupCIServer.class);
    }
    
    public void build(BuildGroupCIServer buildGroupCIServer, String jobName, Map<String, String> parameters) 
            throws JobStartException {        
        log.info("Building job {} in ci server {}", jobName, buildGroupCIServer);        
        try {
            // Get CI Backend base URL 
            String backendURL = sysConfigEJB.getSysConfig(SysConfigKey.BASE_URL).getConfigValue();
            // Check if parameters is null
            if (parameters == null) {
                parameters = new HashMap<String, String>();
            }
            parameters.put(CIParam.BACKEND_URL.toString(), backendURL);
        } catch (NotFoundException e) {
            // ...do not add if the key is not found
            log.warn("Could not find " + SysConfigKey.BASE_URL + " on System Configurations: " + e.getMessage());
        }
        
        CIController ciCtrl = new JenkinsController(buildGroupCIServer);
        try {
            ciCtrl.build(jobName, parameters);
        } catch (CIControllerException ex) {
            throw new JobStartException(ex);
        }
    }    
}
