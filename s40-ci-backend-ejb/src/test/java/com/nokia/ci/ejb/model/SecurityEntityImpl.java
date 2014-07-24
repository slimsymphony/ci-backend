/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.model;

/**
 *
 * @author hhellgre
 */
public class SecurityEntityImpl extends SecurityEntity {

    private Long id;
    private Long projectId;

    public SecurityEntityImpl() {
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public void setProjectId(Long id) {
        this.projectId = id;
    }

    @Override
    public Long getProjectId() {
        return this.projectId;
    }
}
