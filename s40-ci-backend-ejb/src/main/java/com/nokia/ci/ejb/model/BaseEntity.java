/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Entities must extend this class to gain general columns and to enable usage
 * of "CrudFunctionality" in session beans.
 */
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    private String createdBy;
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;
    private String modifiedBy;

    /**
     * Abstract id functionality to provide access for generics based tools.
     *
     * @return Id
     */
    public abstract Long getId();

    /**
     * Abstract id functionality to provide access for generics based tools.
     *
     * @param id
     */
    public abstract void setId(Long id);

    @PrePersist
    public void prePersist() {
        setCreated(new Date());
    }

    @PreUpdate
    public void preUpdate() {
        setModified(new Date());
    }

    /**
     * @return the created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * @param created the created to set
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return the modified
     */
    public Date getModified() {
        return modified;
    }

    /**
     * @param modified the modified to set
     */
    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Override
    public String toString() {
        return getClass().getCanonicalName() + "[id=" + getId() + "]";
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (getId() != null ? getId().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof BaseEntity)) {
            return false;
        }
        BaseEntity other = (BaseEntity) object;
        if (this.getId() == null && other.getId() == null) {
            //Can not use Id equality. Use reference check.
            return super.equals(object);
        }
        if (this.getId() == null || other.getId() == null) {
            return false;
        }
        if (!this.getId().equals(other.getId())) {
            return false;
        }

        return true;
    }
}
