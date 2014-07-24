package com.nokia.ci.ejb.model;

import javax.persistence.MappedSuperclass;

/**
 * Abstract super class for verification configuration entities.
 *
 * @author vrouvine
 */
@MappedSuperclass
public abstract class AbstractVerificationConf extends BaseEntity {

    public abstract Product getProduct();

    public abstract void setProduct(Product product);

    public abstract Verification getVerification();

    public abstract void setVerification(Verification verification);

    public abstract VerificationCardinality getCardinality();
}
