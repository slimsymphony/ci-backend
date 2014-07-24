package com.nokia.ci.client.model;

/**
 * Dummy class without default constructor.
 * @author vrouvine
 */
public class DummyPrivateConstructor {
    
    private int id;
    
    private DummyPrivateConstructor() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
