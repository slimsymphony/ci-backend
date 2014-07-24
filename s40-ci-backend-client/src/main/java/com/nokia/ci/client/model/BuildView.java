package com.nokia.ci.client.model;

import com.nokia.ci.ejb.model.BuildPhase;
import com.nokia.ci.ejb.model.BuildStatus;
import java.util.Date;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * View model for Build.
 * @author vrouvine
 */
@XmlRootElement
public class BuildView extends AbstractView {

    @XmlElement
    private Long id;
    @XmlElement
    private int buildNumber;
    @XmlElement
    private BuildPhase phase;
    @XmlElement
    private BuildStatus status;
    @XmlElement
    private String url;
    @XmlElement
    private Date startTime;
    @XmlElement
    private Date endTime;
    @XmlElement
    private String refSpec;
    @XmlElement
    private String verificationConf;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the number
     */
    public int getBuildNumber() {
        return buildNumber;
    }

    /**
     * @param buildNumber the number to set
     */
    public void setBuildNumber(int buildNumber) {
        this.buildNumber = buildNumber;
    }

    /**
     * @return the phase
     */
    public BuildPhase getPhase() {
        return phase;
    }

    /**
     * @param phase the phase to set
     */
    public void setPhase(BuildPhase phase) {
        this.phase = phase;
    }

    /**
     * @return the status
     */
    public BuildStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(BuildStatus status) {
        this.status = status;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the startTime
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the endTime
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * @return the refSpec
     */
    public String getRefSpec() {
        return refSpec;
    }

    /**
     * @param refSpec the refSpec to set
     */
    public void setRefSpec(String refSpec) {
        this.refSpec = refSpec;
    }

    /**
     * @return the verificationConf
     */
    public String getVerificationConf() {
        return verificationConf;
    }

    /**
     * @param verificationConf the verificationConf to set
     */
    public void setVerificationConf(String verificationConf) {
        this.verificationConf = verificationConf;
    }
}
