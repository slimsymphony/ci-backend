package com.nokia.ci.ui.model;

import org.primefaces.component.menuitem.MenuItem;

/**
 * Relation aware menu item class.
 * This menu item knows its own parent menu item.
 * 
 * @author vrouvine
 */
public class CIMenuItem extends MenuItem {
    
    private boolean parent;
    private Long entityId;
    private Long parentEntityId;
    
    public CIMenuItem() {
        super();
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Long getParentEntityId() {
        return parentEntityId;
    }

    public void setParentEntityId(Long parentEntityId) {
        this.parentEntityId = parentEntityId;
    }

    public boolean hasParent() {
        return parent;
    }

    public void setParent(boolean parent) {
        this.parent = parent;
    }
}
