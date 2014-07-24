package com.nokia.ci.ejb.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entity class for Branch Verification Configuration.
 *
 * @author ttyppo
 */
@Entity
@Table(name = "BRANCH_VERIFICATION_CONF")
public class BranchVerificationConf extends AbstractVerificationConf {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private Branch branch;
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

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
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
