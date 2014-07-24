/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.VerificationFailureReason;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 *
 * @author jajuutin
 */
@Stateless
@LocalBean
public class VerificationFailureReasonEJB extends CrudFunctionality<VerificationFailureReason> {

    public VerificationFailureReasonEJB() {
        super(VerificationFailureReason.class);
    }
}
