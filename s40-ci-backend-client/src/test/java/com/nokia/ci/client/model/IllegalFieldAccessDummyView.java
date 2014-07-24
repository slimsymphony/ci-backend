package com.nokia.ci.client.model;

import javax.xml.bind.annotation.XmlElement;

/**
 * Invalid field access dummy view.
 * @author vrouvine
 */
public class IllegalFieldAccessDummyView extends AbstractView {

    @XmlElement
    private static final int id = 0;

    public int getId() {
        return id;
    }
}
