package com.nokia.ci.ejb.gerrit.model;

import java.util.List;

public class CurrentPatchSet {

    private String number;
    private String revision;
    private String[] parents;
    private String ref;
    private GerritUploader uploader;
    private Long createdOn;
    private List<GerritFile> files;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public GerritUploader getUploader() {
        return uploader;
    }

    public void setUploader(GerritUploader uploader) {
        this.uploader = uploader;
    }

    public Long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Long createdOn) {
        this.createdOn = createdOn;
    }

    public List<GerritFile> getFiles() {
        return files;
    }

    public void setFiles(List<GerritFile> files) {
        this.files = files;
    }

    public String[] getParents() {
        return parents;
    }

    public void setParents(String[] parents) {
        this.parents = parents;
    }
    
}
