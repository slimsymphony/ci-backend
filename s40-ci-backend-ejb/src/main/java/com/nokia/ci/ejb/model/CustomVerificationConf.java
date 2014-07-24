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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entity class of custom verification configuration.
 *
 * @author vrouvine
 */
@Entity
@Table(name = "CUSTOM_VERIFICATION_CONF")
public class CustomVerificationConf extends AbstractCustomVerificationConf {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private JobCustomVerification jobCustomVerification;
    @ManyToOne
    private Product product;
    @Enumerated(EnumType.STRING)
    private VerificationCardinality cardinality = VerificationCardinality.MANDATORY;
    private String imeiCode;
    @Column(length=1024)
    private String tasUrl;
    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "CUSTOM_VERIFICATION_CONF_FILE")
    private List<UserFile> userFiles = new ArrayList<UserFile>();

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public AbstractCustomVerification getCustomVerification() {
        return jobCustomVerification;
    }

    public JobCustomVerification getJobCustomVerification() {
        return jobCustomVerification;
    }

    public void setJobCustomVerification(JobCustomVerification jobCustomVerification) {
        this.jobCustomVerification = jobCustomVerification;
    }
    
    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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
