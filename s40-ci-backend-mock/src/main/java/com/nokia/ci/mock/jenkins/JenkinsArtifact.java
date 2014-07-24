/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.mock.jenkins;


/**
 *
 * @author miikka
 */
public class JenkinsArtifact {

    private String fileName;
    private String relativePath;

    public JenkinsArtifact(String fileName, String relativePath) {
        this.fileName = fileName;
        this.relativePath = relativePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setName(String fileName) {
        this.fileName = fileName;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setUrl(String relativePath) {
        this.relativePath = relativePath;
    }
}
