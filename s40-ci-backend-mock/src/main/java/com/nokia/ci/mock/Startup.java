/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.mock;

import com.nokia.ci.mock.access.ProjectAccessServer;
import com.nokia.ci.mock.gerrit.GerritServer;
import com.nokia.ci.mock.http.HttpServer;
import com.nokia.ci.mock.jenkins.JenkinsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
public class Startup {
    
    private static Logger log = LoggerFactory.getLogger(Startup.class);
    
    public static void main(String[] list) throws Exception {
        log.info("Starting MOCK servers");
        
        JenkinsServer jenkins = new JenkinsServer();
        jenkins.setDaemon(true);
        jenkins.start();
        
        GerritServer gerrit = new GerritServer();
        gerrit.setDaemon(true);
        gerrit.start();
        
        ProjectAccessServer access = new ProjectAccessServer();
        access.setDaemon(true);
        access.start();
        
        HttpServer http = new HttpServer();
        http.setDaemon(true);
        http.start();
    }
}
