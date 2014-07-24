/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.jenkins;

import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author miikka
 */
public class Artifact {

    private String name;
    private URL url;
    private String artifactPath = "";

    public Artifact(String name) {
        this.name = name;
        this.url = null;
    }

    public Artifact(String name, String url) {
        this.name = name;
        try {
            this.url = new URL(url);
        } catch (MalformedURLException ex) {
            this.url = null;
        }
    }

    public Artifact(String name, URL url) {
        this.name = name;
        this.url = url;
    }

    public Artifact(String name, URL url, String artifactPath) {
        this.name = name;
        this.url = url;
        this.artifactPath = artifactPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getArtifactPath() {
        return artifactPath;
    }

    public void setArtifactPath(String artifactPath) {
        this.artifactPath = artifactPath;
    }
}
