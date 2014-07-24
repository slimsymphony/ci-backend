package com.nokia.ci.ejb;

import com.nokia.ci.ejb.event.BuildFinishedEvent;
import com.nokia.ci.ejb.exception.InvalidPhaseException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildPhase;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.model.BuildVerificationConf;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.LockModeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business logic implementation for atomically finalizing {@link Build} object.
 *
 * @author ttyppo
 */
@Stateless
@LocalBean
public class FinalizeBuildEJB extends CrudFunctionality<Build> implements Serializable {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(FinalizeBuildEJB.class);
    @EJB
    BuildGroupEJB buildGroupEJB;
    @EJB
    BuildEJB buildEJB;
    
    @Inject
    @BuildFinishedEvent
    private Event<Long> buildFinishedEvent;

    public FinalizeBuildEJB() {
        super(Build.class);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void finalizeBuildAtomic(Long id, BuildStatus status) throws NotFoundException {
        Build build = read(id);
        buildEJB.updateBuildStatus(id, status);
        
        BuildGroup buildGroup = buildGroupEJB.readWithLock(build.getBuildGroup().getId(), LockModeType.PESSIMISTIC_WRITE);

        // Set build group status and phase accordingly.
        BuildStatus buildGroupStatus = status;
        BuildVerificationConf conf = build.getBuildVerificationConf();
        if (conf.getCardinality() != null) {
            buildGroupStatus = conf.getCardinality().combine(buildGroupStatus);
        }

        try {
            buildGroupEJB.updateStatus(buildGroup.getId(), buildGroupStatus);
        } catch (InvalidPhaseException ex) {
            log.warn("Updating buildGroup status failed! Cause: {}", ex.getMessage());
        }

        buildFinishedEvent.fire(id);
    }
}
