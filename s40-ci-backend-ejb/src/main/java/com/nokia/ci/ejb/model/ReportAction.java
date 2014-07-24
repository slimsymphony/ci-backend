package com.nokia.ci.ejb.model;

import com.nokia.ci.ejb.reportaction.ReportActionStatus;
import com.nokia.ci.ejb.reportaction.ReportActionTitle;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Abstract super class for report action.
 *
 * @author vrouvine
 */
@Entity
@Table(name = "REPORT_ACTION")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ReportAction extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Job job;
    @Enumerated(EnumType.STRING)
    private ReportActionStatus status;
    @Enumerated(EnumType.STRING)
    private ReportActionTitle title;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public ReportActionStatus getStatus() {
        return status;
    }

    public void setStatus(ReportActionStatus status) {
        this.status = status;
    }

    public ReportActionTitle getTitle() {
        return title;
    }

    public void setTitle(ReportActionTitle title) {
        this.title = title;
    }
}
