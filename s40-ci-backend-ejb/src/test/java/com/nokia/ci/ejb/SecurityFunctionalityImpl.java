/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.SecurityEntityImpl;

/**
 *
 * @author hhellgre
 */
public class SecurityFunctionalityImpl extends SecurityFunctionality<SecurityEntityImpl> {

    public SecurityFunctionalityImpl() {
        super(SecurityEntityImpl.class);
    }
}
