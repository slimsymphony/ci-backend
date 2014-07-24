package com.nokia.ci.client.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * View model for project verification configurations.
 * @author vrouvine
 */
@XmlRootElement
public class ProjectVerificationConfView extends AbstractView {
    
    @XmlElement
    private Long id;
    @XmlElement
    private Long projectId;
    @XmlElement
    private Long productId;
    @XmlElement
    private Long verificationId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getVerificationId() {
        return verificationId;
    }

    public void setVerificationId(Long verificationId) {
        this.verificationId = verificationId;
    }
}
