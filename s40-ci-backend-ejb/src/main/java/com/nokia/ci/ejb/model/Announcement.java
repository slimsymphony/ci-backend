package com.nokia.ci.ejb.model;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 10/3/12
 * Time: 12:56 PM
 * To change this template use File | Settings | File Templates.
 */

@MappedSuperclass
public abstract class Announcement extends BaseEntity {

    @Lob
    private String message;
    @Enumerated(EnumType.STRING)
    private AnnouncementType type;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AnnouncementType getType() {
        return type;
    }

    public void setType(AnnouncementType type) {
        this.type = type;
    }

}
