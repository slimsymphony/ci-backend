/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.BaseEntityImpl;

/**
 * Stub for testing abstract base class "CrudFunctionality"
 * 
 * @author jajuutin
 */
public class CrudFunctionalityImpl extends CrudFunctionality<BaseEntityImpl> {

    public CrudFunctionalityImpl() {
        super(BaseEntityImpl.class);
    }   
}
