package com.nokia.ci.ejb.model;

import java.util.List;

import javax.persistence.MappedSuperclass;

/**
 * Abstract super class for template and job verification configuration entities.
 *
 * @author ttyppo
 */
@MappedSuperclass
public abstract class AbstractTemplateVerificationConf extends AbstractVerificationConf {

    public abstract String getImeiCode();
    
    public abstract void setImeiCode(String imeiCode);
    
    public abstract String getTasUrl();
    
    public abstract void setTasUrl(String tasUrl);
    
    public abstract List<UserFile> getUserFiles();
}
