package com.nokia.ci.client.model;

import javax.xml.bind.annotation.XmlElement;

/**
 * Dummy view class for model tests.
 * All fields are annotated with XmlElement annotation.
 * @author vrouvine
 */
public class FullDummyView extends AbstractView {
    
    @XmlElement
    private Long id;
    @XmlElement
    private String name;
    @XmlElement
    private String address;
    @XmlElement
    private DummyEnum dummyEnum;

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public DummyEnum getDummyEnum() {
        return dummyEnum;
    }

    public void setDummyEnum(DummyEnum dummyEnum) {
        this.dummyEnum = dummyEnum;
    }
}
