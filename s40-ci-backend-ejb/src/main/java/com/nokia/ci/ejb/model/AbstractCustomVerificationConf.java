/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.model;

import java.util.List;

import javax.persistence.MappedSuperclass;

/**
 *
 * @author jajuutin
 */
@MappedSuperclass
public abstract class AbstractCustomVerificationConf extends BaseEntity {

    public abstract Product getProduct();
    public abstract void setProduct(Product product);
    public abstract VerificationCardinality getCardinality();
    public abstract AbstractCustomVerification getCustomVerification();
    public abstract String getImeiCode();
    public abstract void setImeiCode(String imeiCode);
    public abstract String getTasUrl();
    public abstract void setTasUrl(String tasUrl);
    public abstract List<UserFile> getUserFiles();
}
