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
 * Entity class for Template Verification Configuration.
 *
 * @author jajuutin
 */
@Entity
@Table(name = "TEMPLATE_VERIFICATION_CONF")
public class TemplateVerificationConf extends AbstractTemplateVerificationConf {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private Template template;
    @ManyToOne
    private Product product;
    @ManyToOne
    private Verification verification;
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

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    @Override
    public VerificationCardinality getCardinality() {
        return cardinality;
    }

    public void setCardinality(VerificationCardinality cardinality) {
        this.cardinality = cardinality;
    }

    @Override
    public String getImeiCode() {
        return imeiCode;
    }

    @Override
    public void setImeiCode(String imeiCode) {
        this.imeiCode = imeiCode;
    }

    @Override
    public String getTasUrl() {
        return tasUrl;
    }

    @Override
    public void setTasUrl(String tasUrl) {
        this.tasUrl = tasUrl;
    }

    @Override
    public List<UserFile> getUserFiles() {
        return null;
    }
}
