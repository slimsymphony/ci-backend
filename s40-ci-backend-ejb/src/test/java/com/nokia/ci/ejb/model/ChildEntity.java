package com.nokia.ci.ejb.model;

/**
 *
 * @author vrouvine
 */
public class ChildEntity extends BaseEntity {
    
    private Long id;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

}
