package com.nokia.ci.ejb.jms;

import com.nokia.ci.ejb.model.Change;
import java.io.Serializable;
import java.util.List;

/**
 * Wrapper object for job start messages.
 * 
 * @author vrouvine
 */
public class JobStartMessage implements Serializable {
    
    private Long jobId;
    private String refspec;
    private String commitId;
    private List<Change> changes;

    public JobStartMessage(Long jobId, String refspec, String commitId, List<Change> changes) {
        this.jobId = jobId;
        this.refspec = refspec;
        this.commitId = commitId;
        this.changes = changes;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getRefspec() {
        return refspec;
    }

    public void setRefspec(String refspec) {
        this.refspec = refspec;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public List<Change> getChanges() {
        return changes;
    }

    public void setChanges(List<Change> changes) {
        this.changes = changes;
    }
}
