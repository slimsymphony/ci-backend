/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.mock.jenkins;

/**
 *
 * @author hhellgre
 */
public class JenkinsSingleBuildConfiguration {

    private Long duration;
    private String status;

    public JenkinsSingleBuildConfiguration() {
    }

    public JenkinsSingleBuildConfiguration(Long duration, String status) {
        this.duration = duration;
        this.status = status;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}