/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author jajuutin
 */
@Entity
@Table(name = "VERIFICATION_FAILURE_REASON")
public class VerificationFailureReason extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    @Lob
    private String description;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Verification verification;
    @Enumerated(EnumType.STRING)
    private VerificationFailureReasonSeverity severity = VerificationFailureReasonSeverity.BLOCKING;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Verification getVerification() {
        return verification;
    }

    public void setVerification(Verification verification) {
        this.verification = verification;
    }

    public VerificationFailureReasonSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(VerificationFailureReasonSeverity severity) {
        this.severity = severity;
    }
}
