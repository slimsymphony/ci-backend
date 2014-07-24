package com.nokia.ci.client.model;

import java.util.List;

/**
 * Dummy class for model tests.
 * This class presents some other model than view model (For example Entity).
 * @author vrouvine
 */
public class Dummy {

    private Long id;
    private String name;
    private String address;
    private List<Integer> list;
    private DummyEnum dummyEnum;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Integer> getList() {
        return list;
    }

    public void setList(List<Integer> list) {
        this.list = list;
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
