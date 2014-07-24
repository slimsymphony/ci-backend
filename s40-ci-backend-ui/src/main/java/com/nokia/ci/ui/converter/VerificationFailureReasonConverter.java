/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.converter;

import com.nokia.ci.ejb.VerificationFailureReasonEJB;
import com.nokia.ci.ejb.model.VerificationFailureReason;
import javax.enterprise.context.RequestScoped;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author hhellgre
 */
@FacesConverter("VerificationFailureReasonConverter")
@RequestScoped
public class VerificationFailureReasonConverter extends EntityConverter<VerificationFailureReason> {

    public VerificationFailureReasonConverter() {
        super(VerificationFailureReasonEJB.class);
    }
}
