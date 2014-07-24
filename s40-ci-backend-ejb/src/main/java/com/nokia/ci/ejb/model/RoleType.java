package com.nokia.ci.ejb.model;

/**
 * Enumeration for system role types.
 * 
 * @author vrouvine
 */
public enum RoleType {
    
    SYSTEM_ADMIN("System Admin"),
    PROJECT_ADMIN("Project Admin"),
    USER("User");
    
    private String label;
    
    private RoleType(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }
}
