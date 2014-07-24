package com.nokia.ci.client.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * View model for Job.
 *
 * @author vrouvine
 */
@XmlRootElement
public class JobView extends AbstractView {

    @XmlElement
    private Long id;
    @XmlElement
    private String name;
    @XmlElement
    private String displayName;
    @XmlElement
    private String url;
    @XmlElement
    private Long branchId;
    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the branchId
     */
    public Long getBranchId() {
        return branchId;
    }

    /**
     * @param branchId the branchId to set
     */
    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }
}
