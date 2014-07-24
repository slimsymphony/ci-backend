package com.nokia.ci.ejb.model;

import javax.persistence.Column;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entity class for Job Verification Configuration.
 *
 * @author vrouvine
 */
@Entity
@Table(name = "JOB_VERIFICATION_CONF")
public class JobVerificationConf extends AbstractTemplateVerificationConf {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private Job job;
    @ManyToOne
    private Product product;
    @ManyToOne
    private Verification verification;
    @Enumerated(EnumType.STRING)
    private VerificationCardinality cardinality = VerificationCardinality.MANDATORY;
    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "JOB_VERIFICATION_CONF_FILE")
    private List<UserFile> userFiles = new ArrayList<UserFile>();

    private String imeiCode;
    @Column(length=1024)
    private String tasUrl;
    
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

    @Override
    public Product getProduct() {
        return product;
    }

    @Override
    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public Verification getVerification() {
        return verification;
    }

    @Override
    public void setVerification(Verification verification) {
        this.verification = verification;
    }
    
    public VerificationCardinality getCardinality() {
        return cardinality;
    }

    public void setCardinality(VerificationCardinality cardinality) {
        this.cardinality = cardinality;
    }

    public String getImeiCode() {
        return imeiCode;
    }

    public void setImeiCode(String imeiCode) {
        this.imeiCode = imeiCode;
    }

    public String getTasUrl() {
        return tasUrl;
    }

    public void setTasUrl(String tasUrl) {
        this.tasUrl = tasUrl;
    }
    
    public List<UserFile> getUserFiles() {
        return userFiles;
    }

    public void setUserFiles(List<UserFile> userFiles) {
        this.userFiles = userFiles;
    }
}
