/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.reportaction;

import com.nokia.ci.ejb.BuildEJB;
import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.ChangeEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.ReportActionEJB;
import com.nokia.ci.ejb.event.BuildFinishedEvent;
import com.nokia.ci.ejb.event.BuildGroupFinishedEvent;
import com.nokia.ci.ejb.event.BuildGroupStartedEvent;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.BranchType;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.model.BuildVerificationConf;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.ReportAction;
import com.nokia.ci.ejb.model.VerificationCardinality;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class ReportActionIgniterEJB {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ReportActionIgniterEJB.class);
    @EJB
    BuildEJB buildEJB;
    @EJB
    BuildGroupEJB buildGroupEJB;
    @EJB
    ReportActionEJB reportActionEJB;
    @EJB
    JobEJB jobEJB;
    @EJB
    ChangeEJB changeEJB;

    @Asynchronous
    public void buildGroupStarted(@Observes(during = TransactionPhase.AFTER_COMPLETION) @BuildGroupStartedEvent Long id) throws NotFoundException {
        igniteNotificationActions(id);
    }

    @Asynchronous
    public void buildFinished(@Observes(during = TransactionPhase.AFTER_SUCCESS) @BuildFinishedEvent Long id)
            throws NotFoundException {
        igniteBuildReportActions(id);
    }

    @Asynchronous
    public void buildGroupFinished(@Observes(during = TransactionPhase.AFTER_SUCCESS) @BuildGroupFinishedEvent Long id)
            throws NotFoundException {
        igniteBuildGroupReportActions(id);
    }

    public void igniteNotificationActions(Long id) throws NotFoundException {

        BuildGroup buildGroup = buildGroupEJB.read(id);
        Job job = buildGroup.getJob();
        Branch branch = job.getBranch();

        if (branch.getType() != BranchType.DEVELOPMENT) {
            return;
        }

        List<ReportAction> notifications = jobEJB.getReportActions(job.getId(), ReportActionStatus.STARTED);
        if (notifications.isEmpty()) {
            return;
        }

        List<Change> changes = buildGroup.getChanges();
        List<Change> unstableChanges = new ArrayList<Change>();
        for (Change change : changes) {

            BuildStatus status;
            try {
                status = changeEJB.getLatestBuildGroupStatus(change.getId(), BranchType.SINGLE_COMMIT);
            } catch (NotFoundException e) {
                log.warn("No SCV results found for change {} while processing notifications", change.getId());
                continue;
            }
            if (BuildStatus.UNSTABLE.equals(status)) {
                unstableChanges.add(change);
            }
        }
        for (ReportAction notification : notifications) {
            reportActionEJB.execute(notification, buildGroup, unstableChanges);
        }

    }

    public void igniteBuildReportActions(Long id) throws NotFoundException {
        Build build = buildEJB.read(id);

        // Filter.
        BuildVerificationConf conf = build.getBuildVerificationConf();
        if (conf.getCardinality() != VerificationCardinality.MANDATORY) {
            return;
        }

        if (build.getStatus() != BuildStatus.FAILURE) {
            return;
        }

        BuildGroup buildGroup = build.getBuildGroup();
        if (buildGroup.getStatus().worstThan(BuildStatus.UNSTABLE)) {
            return;
        }

        Job job = buildGroup.getJob();
        Branch branch = job.getBranch();
        if (branch.getType() != BranchType.SINGLE_COMMIT && 
                branch.getType() != BranchType.TOOLBOX &&
                branch.getType() != BranchType.DRAFT) {
            return;
        }

        log.info("Performing report actions for build {} with status {}", build, build.getStatus());

        List<ReportAction> reportActions = jobEJB.getReportActions(build.getBuildGroup().getJob().getId(),
                build.getStatus().getCorrespondingReportActionStatus());

        for (ReportAction action : reportActions) {
            reportActionEJB.execute(action, build);
        }
    }

    public void igniteBuildGroupReportActions(Long id) throws NotFoundException {
        BuildGroup buildGroup = buildGroupEJB.read(id);

        log.info("Performing report actions for build group {} with status {}", buildGroup, buildGroup.getStatus());
        List<ReportAction> reportActions = jobEJB.getReportActions(buildGroup.getJob().getId(), buildGroup.getStatus().getCorrespondingReportActionStatus());

        reportActions = resolveActionsToBeExecuted(reportActions, buildGroup);
        for (ReportAction action : reportActions) {
            reportActionEJB.execute(action, buildGroup);
        }
    }

    /**
     * One status can have multiple, mutually exclusive actions. Resolve
     * action(s) to be executed based on the properties of the buildgroup.
     *
     * @param reportActions
     * @param status
     * @return
     */
    private List<ReportAction> resolveActionsToBeExecuted(List<ReportAction> reportActions, BuildGroup buildGroup) {

        if (reportActions.isEmpty() || buildGroup.getBranchType() != BranchType.SINGLE_COMMIT || buildGroup.getStatus() != BuildStatus.UNSTABLE) {
            return reportActions;
        }

        Map<ReportActionTitle, ReportAction> reportActionMap = new HashMap();
        for (ReportAction ra : reportActions) {
            reportActionMap.put(ra.getTitle(), ra);
        }

        boolean classifiedOk = false;
        try {
            classifiedOk = buildGroupEJB.isClassifiedOk(buildGroup.getId());
        } catch (NotFoundException e) {
            log.warn("Could not resolve classification for all builds in buildGroup id={}", buildGroup.getId());
        }

        if (reportActionMap.containsKey(ReportActionTitle.UNSTABLE_NO_BLOCKING) && classifiedOk) {
            reportActionMap.remove(ReportActionTitle.UNSTABLE);
        } else {
            reportActionMap.remove(ReportActionTitle.UNSTABLE_NO_BLOCKING);
        }
        return new ArrayList<ReportAction>(reportActionMap.values());
    }
}
