package com.nokia.ci.ejb.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Entity class for verification input parameters.
 * 
 * @author vrouvine
 */
@Entity
@Table(name = "RELEASE")
public class Release extends BaseEntity {
    
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;    
    @Temporal(TemporalType.TIMESTAMP)
    private Date releaseTime;    
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "BUILDGROUP_ID")
    private BuildGroup buildGroup;    

    /**
     * @return the id
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    @Override
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the releaseTime
     */
    public Date getReleaseTime() {
        return releaseTime;
    }

    /**
     * @param releaseTime the releaseTime to set
     */
    public void setReleaseTime(Date releaseTime) {
        this.releaseTime = releaseTime;
    }

    /**
     * @return the buildGroup
     */
    public BuildGroup getBuildGroup() {
        return buildGroup;
    }

    /**
     * @param buildGroup the buildGroup to set
     */
    public void setBuildGroup(BuildGroup buildGroup) {
        this.buildGroup = buildGroup;
    }
}
