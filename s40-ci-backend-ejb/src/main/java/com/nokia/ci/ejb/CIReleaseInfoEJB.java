/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.CIReleaseInfo;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 *
 * @author hhellgre
 */
@Stateless
@LocalBean
public class CIReleaseInfoEJB extends CrudFunctionality<CIReleaseInfo> {

    public CIReleaseInfoEJB() {
        super(CIReleaseInfo.class);
    }
}
