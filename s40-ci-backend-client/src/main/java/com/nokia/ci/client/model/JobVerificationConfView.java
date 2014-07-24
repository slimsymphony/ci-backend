/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.client.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author jajuutin
 */
@XmlRootElement
public class JobVerificationConfView extends AbstractView {

    @XmlElement
    private Long id;
    @XmlElement
    private Long productId;
    @XmlElement
    private Long verificationId;
    @XmlElement
    private Long jobId;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the productId
     */
    public Long getProductId() {
        return productId;
    }

    /**
     * @param productId the productId to set
     */
    public void setProductId(Long productId) {
        this.productId = productId;
    }

    /**
     * @return the verificationId
     */
    public Long getVerificationId() {
        return verificationId;
    }

    /**
     * @param verificationId the verificationId to set
     */
    public void setVerificationId(Long verificationId) {
        this.verificationId = verificationId;
    }

    /**
     * @return the jobId
     */
    public Long getJobId() {
        return jobId;
    }

    /**
     * @param jobId the jobId to set
     */
    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }
}
