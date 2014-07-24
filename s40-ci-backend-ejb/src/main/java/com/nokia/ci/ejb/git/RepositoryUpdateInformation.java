package com.nokia.ci.ejb.git;

import java.util.concurrent.Future;

/**
 *
 * @author vrouvine
 */
public class RepositoryUpdateInformation {

    private String URL;
    private String localPath;
    private Future<String> action;
    private long branchId;
    private long startTime;

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public Future<String> getAction() {
        return action;
    }

    public void setAction(Future<String> action) {
        this.action = action;
    }

    public long getBranchId() {
        return branchId;
    }

    public void setBranchId(long branchId) {
        this.branchId = branchId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}