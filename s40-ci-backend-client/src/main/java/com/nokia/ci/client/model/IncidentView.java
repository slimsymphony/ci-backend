package com.nokia.ci.client.model;

import com.nokia.ci.ejb.model.IncidentType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 4/8/13
 * Time: 10:52 AM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
public class IncidentView extends AbstractView {

    @XmlElement
    private Long id;
    @XmlElement
    private Date time;
    @XmlElement
    private IncidentType type;
    @XmlElement
    private String description;
    @XmlElement
    private Date checkTime;
    @XmlElement
    private String checkUser;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public IncidentType getType() {
        return type;
    }

    public void setType(IncidentType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(Date checkTime) {
        this.checkTime = checkTime;
    }

    public String getCheckUser() {
        return checkUser;
    }

    public void setCheckUser(String checkUser) {
        this.checkUser = checkUser;
    }
}
