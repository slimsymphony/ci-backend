package com.nokia.ci.ejb.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entity class of custom verification configuration.
 *
 * @author jajuutin
 */
@Entity
@Table(name = "TEMPLATE_CUST_VER_CONF")
public class TemplateCustomVerificationConf extends AbstractCustomVerificationConf {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private TemplateCustomVerification customVerification;
    @ManyToOne
    private Product product;
    @Enumerated(EnumType.STRING)
    private VerificationCardinality cardinality = VerificationCardinality.MANDATORY;
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

    public TemplateCustomVerification getCustomVerification() {
        return customVerification;
    }

    public void setCustomVerification(TemplateCustomVerification customVerification) {
        this.customVerification = customVerification;
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

    @Override
    public List<UserFile> getUserFiles() {
        return null;
    }
    
}
