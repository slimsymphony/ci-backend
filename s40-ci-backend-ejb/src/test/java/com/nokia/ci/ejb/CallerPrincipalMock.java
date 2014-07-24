/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import java.security.Principal;

/**
 *
 * @author hhellgre
 */
public class CallerPrincipalMock implements Principal {

    private String name = "anonymous";

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
