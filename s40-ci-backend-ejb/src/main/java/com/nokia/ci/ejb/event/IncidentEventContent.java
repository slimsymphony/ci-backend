package com.nokia.ci.ejb.event;

import com.nokia.ci.ejb.model.IncidentType;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 4/5/13
 * Time: 12:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class IncidentEventContent {
    private String description;
    private IncidentType type;

    public IncidentEventContent(IncidentType type, String description) {
        this.type=type;
        this.description=description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IncidentType getType() {
        return type;
    }

    public void setType(IncidentType type) {
        this.type = type;
    }
}
