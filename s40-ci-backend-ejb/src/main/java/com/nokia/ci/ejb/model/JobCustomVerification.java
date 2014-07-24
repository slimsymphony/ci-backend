package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Entity class of job custom verification.
 *
 * @author vrouvine
 */
@Entity
@Table(name = "JOB_CUSTOM_VERIFICATION")
public class JobCustomVerification extends AbstractCustomVerification {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Lob
    private String description;
    @ManyToOne
    private Job job;
    @ManyToOne
    private Verification verification;
    @OneToMany(mappedBy = "jobCustomVerification", cascade = CascadeType.ALL)
    private List<CustomVerificationConf> customVerificationConfs = new ArrayList<CustomVerificationConf>();
    @OneToMany(mappedBy = "customVerification", cascade = CascadeType.ALL)
    private List<CustomVerificationParam> customVerificationParams = new ArrayList<CustomVerificationParam>();
    
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    @Override
    public Verification getVerification() {
        return verification;
    }

    @Override
    public void setVerification(Verification verification) {
        this.verification = verification;
    }

    public List<CustomVerificationConf> getCustomVerificationConfs() {
        return customVerificationConfs;
    }

    public void setCustomVerificationConfs(List<CustomVerificationConf> customVerificationConfs) {
        this.customVerificationConfs = customVerificationConfs;
    }

    public List<CustomVerificationParam> getCustomVerificationParams() {
        return customVerificationParams;
    }

    public void setCustomVerificationParams(List<CustomVerificationParam> customVerificationParams) {
        this.customVerificationParams = customVerificationParams;
    }
    
    @Override
    public List<? extends AbstractCustomParam> getAbstractCustomParams() {
        return customVerificationParams;
    }
}
