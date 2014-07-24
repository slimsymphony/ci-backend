package com.nokia.ci.ui.converter;

import com.nokia.ci.ejb.VerificationEJB;
import com.nokia.ci.ejb.model.Verification;
import javax.enterprise.context.RequestScoped;
import javax.faces.convert.FacesConverter;

/**
 * Converter for verification entities.
 *
 * @author vrouvine
 */
@RequestScoped
@FacesConverter("VerificationConverter")
public class VerificationConverter extends EntityConverter<Verification> {

    public VerificationConverter() {
        super(VerificationEJB.class);
    }
}
