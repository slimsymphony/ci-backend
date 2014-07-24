/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.CIFaq;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 *
 * @author hhellgre
 */
@Stateless
@LocalBean
public class CIFaqEJB extends CrudFunctionality<CIFaq> {

    public CIFaqEJB() {
        super(CIFaq.class);
    }
}
