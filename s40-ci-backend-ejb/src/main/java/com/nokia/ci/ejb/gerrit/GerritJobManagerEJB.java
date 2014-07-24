/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.gerrit;

import com.nokia.ci.ejb.ChangeEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.FileTriggerPatternEJB;
import com.nokia.ci.ejb.exception.JobStartException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.gerrit.model.GerritDetail;
import com.nokia.ci.ejb.gerrit.model.GerritDetailType;
import com.nokia.ci.ejb.jms.JobStartProducer;
import com.nokia.ci.ejb.model.BranchType;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.ChangeStatus;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.JobTriggerScope;
import com.nokia.ci.ejb.model.JobTriggerType;
import com.nokia.ci.ejb.util.GerritUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@Stateless
@LocalBean
public class GerritJobManagerEJB {

    private static Logger log = LoggerFactory.getLogger(GerritJobManagerEJB.class);
    @Inject
    private JobStartProducer producer;
    @EJB
    private JobEJB jobEJB;
    @EJB
    private ChangeEJB changeEJB;
    @EJB
    private FileTriggerPatternEJB fileTriggerPatternEJB;

    public GerritJobManagerEJB() {
    }

    public List<Job> startJobs(List<GerritDetail> changes, JobTriggerType... jobTriggerTypes) {

        List<Job> startedJobs = new ArrayList<Job>();

        if (changes == null || changes.isEmpty()) {
            return startedJobs;
        }

        // Get all jobs with specified trigger type
        List<Job> jobs = new ArrayList<Job>();
        for (JobTriggerType type : jobTriggerTypes) {
            jobs.addAll(jobEJB.getJobsByTriggerType(type));
        }

        if (jobs == null || jobs.isEmpty()) {
            return startedJobs;
        }

        for (GerritDetail d : changes) {
            for (Job j : jobs) {
                if (j.getBranch() == null
                        || j.getBranch().getName() == null
                        || !j.getBranch().getName().equals(d.getBranch())) {
                    continue;
                }

                if (j.getBranch().getProject() == null
                        || j.getBranch().getProject().getGerritProject() == null
                        || !j.getBranch().getProject().getGerritProject().equals(d.getProject())) {
                    continue;
                }

                if (GerritDetailType.fromStatus(d.getStatus()) == GerritDetailType.NEW
                        && (j.getBranch().getType() != BranchType.TOOLBOX && j.getBranch().getType() != BranchType.SINGLE_COMMIT)) {
                    continue;
                }

                if (GerritDetailType.fromStatus(d.getStatus()) == GerritDetailType.DRAFT && j.getBranch().getType() != BranchType.DRAFT) {
                    continue;
                }

                // Check if job trigger type is for user's own changes only
                if (j.getTriggerScope().equals(JobTriggerScope.USER)) {
                    // Check if change owner is the same as job owner
                    if (!checkChangeOwnerIsSameAsJobOwner(d, j)) {
                        continue;
                    }
                }

                if (startJob(j, d)) {
                    startedJobs.add(j);
                }
            }
        }

        return startedJobs;
    }

    private boolean checkChangeOwnerIsSameAsJobOwner(GerritDetail detail, Job job) {
        if (job.getOwner() == null) {
            return false;
        }
        if (detail.getOwner() == null) {
            return false;
        }
        if (StringUtils.isEmpty(detail.getOwner().getEmail())) {
            return false;
        }
        if (StringUtils.isEmpty(job.getOwner().getEmail())) {
            return false;
        }
        if (!detail.getOwner().getEmail().equalsIgnoreCase(job.getOwner().getEmail())) {
            return false;
        }
        return true;
    }

    public Boolean startJob(Job j, GerritDetail d) {
        if (d == null || j == null) {
            log.error("Cannot start job {} with gerrit detail {}", j, d);
            return false;
        }

        try {
            // One change / GerritDetail
            Change change = new Change();
            change.setProjectId(j.getBranch().getProject().getId());
            change.setAuthorEmail(d.getOwner().getEmail());
            change.setAuthorName(d.getOwner().getName());
            change.setUrl(d.getUrl());

            if (d.getLastUpdated() != null) {
                change.setCommitTime(new Date(d.getLastUpdated()));
            } else if (d.getCreatedOn() != null) {
                change.setCommitTime(new Date(d.getCreatedOn()));
            } else {
                change.setCommitTime(new Date());
            }

            change.setMessage(d.getSubject());
            change.setSubject(d.getSubject());

            if (d.getCurrentPatchSet() != null) {
                change.setCommitId(d.getCurrentPatchSet().getRevision());
                change.setChangeFiles(GerritUtils.getChangeFiles(change, d.getCurrentPatchSet()));
                change.setParentChanges(getParentChanges(change, d.getCurrentPatchSet().getParents()));
            } else if (d.getPatchSet() != null) {
                change.setCommitId(d.getPatchSet().getRevision());
                change.setChangeFiles(GerritUtils.getChangeFiles(change, d.getPatchSet()));
                change.setParentChanges(getParentChanges(change, d.getPatchSet().getParents()));
            } else {
                log.error("Could not start jobs for gerritDetail {} (no revision found)", d);
                return false;
            }

            if (!StringUtils.isEmpty(d.getStatus())) {
                ChangeStatus status;
                try {
                    status = ChangeStatus.valueOf(d.getStatus().toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.info("Could not find change status with name " + d.getStatus());
                    status = ChangeStatus.OPEN;
                }
                change.setStatus(status);
            }

            //File based triggering check
            List<Change> changes = new ArrayList<Change>();
            changes.add(change);
            if (!jobEJB.getFileTriggerPatterns(j.getId()).isEmpty()) {
                boolean matched = fileTriggerPatternEJB.checkFilePathMatch(j.getId(), changes);
                if (!matched) {
                    log.info("Could not match any trigger file path, abandoning change {}", d);
                    return false;
                }
            }

            String ref = null;
            if (d.getCurrentPatchSet() != null) {
                ref = d.getCurrentPatchSet().getRef();
            } else if (d.getPatchSet() != null) {
                ref = d.getPatchSet().getRef();
            } else {
                log.error("Could not determine patchet ref, abandoning change {}", d);
                return false;
            }

            String revision = null;
            if (d.getCurrentPatchSet() != null) {
                revision = d.getCurrentPatchSet().getRevision();
            } else if (d.getPatchSet() != null) {
                revision = d.getPatchSet().getRevision();
            } else {
                log.error("Could not determine patchet revision, abandoning change {}", d);
                return false;
            }

            List<Change> chs = new ArrayList<Change>();
            chs.add(change);
            producer.sendJobStart(j.getId(), ref, revision, chs);
            return true;
        } catch (JobStartException e) {
            log.error("Could not add job {} to producer", j);
            return false;
        }
    }

    private List<Change> getParentChanges(Change change, String[] parentRevisions) {
        List<Change> parentChanges = new ArrayList<Change>();
        if (parentRevisions != null && parentRevisions.length > 0) {
            for (String parent : parentRevisions) {
                Change parentChange = null;
                try {
                    parentChange = changeEJB.getChangeByCommitId(parent);
                } catch (NotFoundException ex) {
                    continue;
                }
                parentChanges.add(parentChange);
            }
        }
        return parentChanges;
    }
}
