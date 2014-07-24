/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.model;

import javax.persistence.MappedSuperclass;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.bridge.builtin.LongBridge;

/**
 *
 * @author hhellgre
 */
@MappedSuperclass
public abstract class SecurityEntity extends BaseEntity {

    @Field(name = "projectId")
    @FieldBridge(impl = LongBridge.class)
    private Long projectId;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
