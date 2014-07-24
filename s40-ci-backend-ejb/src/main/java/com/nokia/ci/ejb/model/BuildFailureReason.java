/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author hhellgre
 */
@Entity
@Table(name = "BUILD_FAILURE_REASON")
public class BuildFailureReason extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "BUILDFAILURE_ID")
    private BuildFailure buildFailure;
    @Column(length = 4000)
    private String name;
    @Column(length = 4000)
    private String description;
    @Column(length = 4000)
    private String failComment;
    private String checkUser;
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

    public BuildFailure getBuildFailure() {
        return buildFailure;
    }

    public void setBuildFailure(BuildFailure buildFailure) {
        this.buildFailure = buildFailure;
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

    public String getFailComment() {
        return failComment;
    }

    public void setFailComment(String comment) {
        this.failComment = comment;
    }

    public VerificationFailureReasonSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(VerificationFailureReasonSeverity severity) {
        this.severity = severity;
    }

    public String getCheckUser() {
        return checkUser;
    }

    public void setCheckUser(String checkUser) {
        this.checkUser = checkUser;
    }
}
