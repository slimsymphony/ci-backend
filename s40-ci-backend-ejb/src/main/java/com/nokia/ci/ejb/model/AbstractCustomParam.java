package com.nokia.ci.ejb.model;

import javax.persistence.MappedSuperclass;

/**
 * Base class for custom parameter.
 * 
 * @author jajuutin
 */
@MappedSuperclass
public abstract class AbstractCustomParam extends BaseEntity {
    public abstract String getParamValue();
    public abstract void setParamValue(String paramValue);
    public abstract CustomParam getCustomParam();
    public abstract void setCustomParam(CustomParam customParam);
}