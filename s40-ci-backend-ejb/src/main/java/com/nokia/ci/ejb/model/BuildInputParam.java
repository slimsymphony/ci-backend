package com.nokia.ci.ejb.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entity class for build input parameters.
 *
 * @author vrouvine
 */
@Entity
@Table(name = "BUILD_INPUT_PARAM")
public class BuildInputParam extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String paramKey;
    private String paramValue;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private BuildVerificationConf buildVerificationConf;

    @Override
    public Long getId() {
        return id;
    }

    @Override
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
