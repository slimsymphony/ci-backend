/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.changetracking;

import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.event.BuildGroupFinishedEvent;
import com.nokia.ci.ejb.event.BuildGroupReleasedEvent;
import com.nokia.ci.ejb.event.BuildGroupStartedEvent;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.BranchType;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.model.ChangeTracker;
import java.util.Collection;
import java.util.Date;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 */
@Stateless
@LocalBean
public class ChangeTrackingEJB {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ChangeTrackingEJB.class);
    
    @EJB
    public BuildGroupEJB buildGroupEJB;
    @EJB
    public ProjectEJB projectEJB;
       
    @Asynchronous
    public void buildGroupStarted(@Observes(during = TransactionPhase.AFTER_SUCCESS) @BuildGroupStartedEvent Long id) throws NotFoundException {
        log.info("Recording build group start: {}", id);
        
        BuildGroup buildGroup = buildGroupEJB.read(id);

        Branch branch = buildGroup.getJob().getBranch();
        if (!isSupported(branch)) {
            return;
        }

        Collection<ChangeTracker> cts = projectEJB.getChangeTrackers(branch.getProject().getId(),
                buildGroup.getChanges());

        for (ChangeTracker ct : cts) {
            if (branch.getType() == BranchType.SINGLE_COMMIT && ct.getScvStart() == null) {
                ct.setScvStart(new Date());
            } else if (branch.getType() == BranchType.DEVELOPMENT && ct.getDbvStart() == null) {
                ct.setDbvStart(new Date());
            } else if (branch.getType() == BranchType.MASTER && ct.getMasterStart() == null) {
                ct.setMasterStart(new Date());
            }
        }
    }

    @Asynchronous
    public void buildGroupFinished(@Observes(during = TransactionPhase.AFTER_SUCCESS) @BuildGroupFinishedEvent Long id) throws NotFoundException {       
        log.info("Recording build group finish: {}", id);

        BuildGroup buildGroup = buildGroupEJB.read(id);

        boolean passed = false;
        
        if(buildGroup.getStatus() == BuildStatus.SUCCESS || 
                buildGroup.getStatus() == BuildStatus.UNSTABLE) {
            passed = true;
        }
        
        if(buildGroup.getJob() == null) {
            return;
        }
        
        Branch branch = buildGroup.getJob().getBranch();
        if (!isSupported(branch)) {
            return;
        }
        
        if(branch.getProject() == null) {
            return;
        }

        Collection<ChangeTracker> cts = projectEJB.getChangeTrackers(branch.getProject().getId(),
                buildGroup.getChanges());

        for (ChangeTracker ct : cts) {
            if (branch.getType() == BranchType.SINGLE_COMMIT && ct.getScvEnd() == null) {
                if (passed) {
                    ct.setScvEnd(new Date());
                } else {
                    ct.setScvStart(null);
                    ct.setScvEnd(null);
                }
            } else if (branch.getType() == BranchType.DEVELOPMENT && ct.getDbvEnd() == null) {
                if (passed) {
                    ct.setDbvEnd(new Date());
                } else {
                    ct.setDbvStart(null);
                    ct.setDbvEnd(null);
                }
            } else if (branch.getType() == BranchType.MASTER && ct.getMasterEnd() == null) {
                if (passed) {
                    ct.setMasterEnd(new Date());
                } else {
                    ct.setMasterStart(null);
                    ct.setMasterEnd(null);
                }
            }
        }
    }

    @Asynchronous
    public void buildGroupReleased(@Observes(during = TransactionPhase.AFTER_SUCCESS) @BuildGroupReleasedEvent Long id) throws NotFoundException {
        log.info("Recording build group release: {}", id);
        
        BuildGroup buildGroup = buildGroupEJB.read(id);

        Branch branch = buildGroup.getJob().getBranch();
        if (!isSupported(branch)) {
            return;
        }

        Collection<ChangeTracker> cts = projectEJB.getChangeTrackers(branch.getProject().getId(),
                buildGroup.getChanges());

        for (ChangeTracker ct : cts) {
            ct.setReleased(new Date());
        }
    }
    
    private boolean isSupported(Branch branch) {
        if (branch == null || branch.getType() == BranchType.TOOLBOX || branch.getType() == BranchType.DRAFT ){
            return false;
        }

        return true;
    }    
}
