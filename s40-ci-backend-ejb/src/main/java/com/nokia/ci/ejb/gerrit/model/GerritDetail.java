package com.nokia.ci.ejb.gerrit.model;

import java.io.Serializable;

public class GerritDetail implements Serializable {

    private String project;
    private String branch;
    private String id;
    private String number;
    private String subject;
    private Owner owner;
    private String url;
    private Long createdOn;
    private Long lastUpdated;
    private String sortKey;
    private Boolean open;
    private String status;
    private CurrentPatchSet currentPatchSet;
    private CurrentPatchSet patchSet;

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Get create time in seconds.
     * 
     * @return create time in seconds
     */
    public Long getCreatedOn() {
        return createdOn;
    }

    /**
     * Set create time in seconds.
     * 
     * @param createdOn create time in seconds
     */
    public void setCreatedOn(Long createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * Get last update time in seconds.
     * 
     * @return last update time in seconds
     */
    public Long getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Set last update time in seconds.
     * 
     * @param lastUpdated last update time in seconds
     */
    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getSortKey() {
        return sortKey;
    }

    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    public Boolean getOpen() {
        return open;
    }

    public void setOpen(Boolean open) {
        this.open = open;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public CurrentPatchSet getCurrentPatchSet() {
        return currentPatchSet;
    }

    public void setCurrentPatchSet(CurrentPatchSet currentPatchSet) {
        this.currentPatchSet = currentPatchSet;
    }
    
    public CurrentPatchSet getPatchSet() {
        return patchSet;
    }

    public void setPatchSet(CurrentPatchSet patchSet) {
        this.patchSet = patchSet;
    }

    @Override
    public String toString() {
        return "GerritDetail{" + "project=" + project + ", branch=" + branch + ", id=" + id + ", number=" + number + ", sortKey=" + sortKey + ", open=" + open + ", status=" + status + '}';
    }
}
