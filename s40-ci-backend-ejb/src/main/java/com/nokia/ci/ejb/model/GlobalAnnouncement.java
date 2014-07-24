package com.nokia.ci.ejb.model;

import javax.persistence.*;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 9/27/12
 * Time: 10:47 AM
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "GLOBAL_ANNOUNCEMENT")
public class GlobalAnnouncement extends Announcement {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


}
