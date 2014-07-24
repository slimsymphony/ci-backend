package com.nokia.ci.ejb.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entity class for Project Verification Configuration.
 *
 * @author vrouvine
 */
@Entity
@Table(name = "PROJECT_VERIFICATION_CONF")
public class ProjectVerificationConf extends AbstractVerificationConf {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private Project project;
    @ManyToOne
    private Product product;
    @ManyToOne
    private Verification verification;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
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
    
    @Override
    public VerificationCardinality getCardinality() {
        return null;
    }
}
