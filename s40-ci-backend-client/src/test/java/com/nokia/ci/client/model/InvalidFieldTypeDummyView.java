package com.nokia.ci.client.model;

import javax.xml.bind.annotation.XmlElement;

/**
 * Dummy view class for model tests.
 * One field type is not matching with {@link Dummy} class field.
 * @author vrouvine
 */
public class InvalidFieldTypeDummyView extends AbstractView {
    
    @XmlElement
    private Long id;
    @XmlElement
    private Long name;
    @XmlElement
    private String address;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getName() {
        return name;
    }

    public void setName(Long name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
