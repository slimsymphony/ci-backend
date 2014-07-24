package com.nokia.ci.client.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * View model for Project.
 * @author vrouvine
 */
@XmlRootElement
public class ProjectView extends AbstractView {
    
    @XmlElement
    private Long id;
    @XmlElement
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
