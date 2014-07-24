/*
 * Build custom parameter entity.
 */
package com.nokia.ci.ejb.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author jajuutin
 */
@Entity
@Table(name = "BUILD_CUSTOM_PARAMETER")
public class BuildCustomParameter extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String paramKey;
    private String paramValue;
    @ManyToOne
    @JoinColumn(nullable = false)
    private BuildVerificationConf buildVerificationConf;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public BuildVerificationConf getBuildVerificationConf() {
        return buildVerificationConf;
    }

    public void setBuildVerificationConf(BuildVerificationConf buildVerificationConf) {
        this.buildVerificationConf = buildVerificationConf;
    }
}
