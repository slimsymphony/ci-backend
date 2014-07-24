package com.nokia.ci.client.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * View model for Branch.
 *
 * @author vrouvine
 */
@XmlRootElement
public class BranchView extends AbstractView {

    @XmlElement
    private Long id;
    @XmlElement
    private String name;
    @XmlElement
    private Long projectId;

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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
