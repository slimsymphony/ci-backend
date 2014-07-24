package com.nokia.ci.ejb.gerrit.model;

public class GerritUploader {

    private String gerritName;
    private String gerritEmail;

    public String getGerritName() {
        return gerritName;
    }

    public void setGerritName(String name) {
        this.gerritName = name;
    }

    public String getGerritEmail() {
        return gerritEmail;
    }

    public void setGerritEmail(String email) {
        this.gerritEmail = email;
    }
}
