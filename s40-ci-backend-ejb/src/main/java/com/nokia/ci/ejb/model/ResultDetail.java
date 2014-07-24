package com.nokia.ci.ejb.model;

import javax.persistence.MappedSuperclass;

/**
 * Super class for Result Detail entities.
 *
 * @author ttyppo
 */
@MappedSuperclass
public abstract class ResultDetail extends BaseEntity {

    private String paramKey;
    private String paramValue;
    private String displayName;
    private String description;

    public String getParamKey() {
        return paramKey;
    }

    public void setParamKey(String paramKey) {
        this.paramKey = paramKey;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
