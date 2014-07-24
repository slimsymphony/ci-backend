/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.model;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

/**
 *
 * @author hhellgre
 */
@MappedSuperclass
public abstract class JobPrePostVerification extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Job job;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private Verification verification;
    private Long verificationOrdinality;

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Verification getVerification() {
        return verification;
    }

    public void setVerification(Verification verification) {
        this.verification = verification;
    }

    public Long getVerificationOrdinality() {
        return verificationOrdinality;
    }

    public void setVerificationOrdinality(Long verificationOrdinality) {
        this.verificationOrdinality = verificationOrdinality;
    }
}
