package com.nokia.ci.ui.model;

/**
 * Projects menu item Enumeration class.
 * 
 * @author vrouvine
 */
public enum ProjectsMenu {
    
    HOME("/secure/pages/projects.xhtml", "", false),
    PROJECT("/secure/pages/projectDetails.xhtml?projectId=%d", "Project %s", false),
    VERIFICATION("/secure/pages/verificationDetails.xhtml?verificationId=%d", "Verification %s", true),
    BUILD("/secure/pages/buildDetails.xhtml?buildId=%d", "Build %s", true);
    
    private String baseUrl;
    private String labelPrefix;
    private boolean parent;
    
    private ProjectsMenu(String baseUrl, String labelPrefix, boolean parent) {
        this.baseUrl = baseUrl;
        this.labelPrefix = labelPrefix;
        this.parent = parent;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getLabelPrefix() {
        return labelPrefix;
    }
    
    public boolean hasParent() {
        return parent;
    }
    
    public static int indexOf(ProjectsMenu menu) {
        int index = 0;
        for(ProjectsMenu pm : values()) {
            if(pm.equals(menu)) {
                break;
            }
            index++;
        }
        return index;
    }
}
