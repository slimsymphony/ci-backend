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
 * Entity class for build result details parameters.
 *
 * @author ttyppo
 */
@Entity
@Table(name = "BUILD_RESULT_DETAILS_PARAM")
public class BuildResultDetailsParam extends ResultDetail {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
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

    public BuildVerificationConf getBuildVerificationConf() {
        return buildVerificationConf;
    }

    public void setBuildVerificationConf(BuildVerificationConf buildVerificationConf) {
        this.buildVerificationConf = buildVerificationConf;
    }

}
