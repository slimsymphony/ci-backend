/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.util;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.CIServer;
import com.nokia.ci.ejb.model.Gerrit;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.Project;
import java.util.List;

/**
 *
 * @author jajuutin
 * 
 * This class is used for validating database entities.
 * To succeed it is required that all entities and relations must be fulfilled
 * backwards. Starting from bottom to up.
 */
public class ConsistencyValidator {

    public static void validate(Project project) throws NotFoundException {
        Gerrit gerrit = project.getGerrit();
        if (gerrit == null) {
            throw new NotFoundException("Gerrit not found for project: " + gerrit);
        }        
    }
    
    public static void validate(Branch branch) throws NotFoundException {
        Project project = branch.getProject();
        if (project == null) {
            throw new NotFoundException("Project not found for branch: " + branch);
        }
        
        List<CIServer> ciServers = branch.getCiServers();
        if(ciServers.isEmpty()) {
            throw new NotFoundException("No CIServers configured for branch: " + branch);
        }
        
        validate(project);
    }
    
    public static void validate(Job job) throws NotFoundException {
        Branch branch = job.getBranch();
        if (branch == null) {
            throw new NotFoundException("Branch not found for job: " + job);
        }
        
        validate(branch);
    }
    
    public static void validate(BuildGroup buildGroup) throws NotFoundException {
        Job job = buildGroup.getJob();
        if (job == null) {
            throw new NotFoundException("Job not found for buildGroup: " + buildGroup);
        }
        
        validate(job);
    }

    public static void validate(Build build) throws NotFoundException {
        BuildGroup buildGroup = build.getBuildGroup();
        if (buildGroup == null) {
            throw new NotFoundException("BuildGroup not found for build: " + build);
        }

        validate(buildGroup);
    }
}
