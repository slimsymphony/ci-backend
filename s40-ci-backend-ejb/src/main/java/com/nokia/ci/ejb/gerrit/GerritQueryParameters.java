package com.nokia.ci.ejb.gerrit;

import java.util.HashSet;
import java.util.Set;

public class GerritQueryParameters {

    private Long gerritId;
    private String projectName;
    private String branchName;
    private Set<String> startedChanges = new HashSet<String>();

    private GerritQueryParameters() {
    }

    public GerritQueryParameters(long gerritId, String projectName, String branchName) {
        this.gerritId = gerritId;
        this.projectName = projectName;
        this.branchName = branchName;
    }

    public Long getGerritId() {
        return gerritId;
    }

    public void setGerritId(Long gerritId) {
        this.gerritId = gerritId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public Set<String> getStartedChanges() {
        return startedChanges;
    }

    public void setStartedChanges(Set<String> startedChanges) {
        this.startedChanges = startedChanges;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((branchName == null) ? 0 : branchName.hashCode());
        result = prime * result + ((gerritId == null) ? 0 : gerritId.hashCode());
        result = prime * result + ((projectName == null) ? 0 : projectName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GerritQueryParameters other = (GerritQueryParameters) obj;
        if (branchName == null) {
            if (other.branchName != null) {
                return false;
            }
        } else if (!branchName.equals(other.branchName)) {
            return false;
        }
        if (gerritId == null) {
            if (other.gerritId != null) {
                return false;
            }
        } else if (!gerritId.equals(other.gerritId)) {
            return false;
        }
        if (projectName == null) {
            if (other.projectName != null) {
                return false;
            }
        } else if (!projectName.equals(other.projectName)) {
            return false;
        }
        return true;
    }
}