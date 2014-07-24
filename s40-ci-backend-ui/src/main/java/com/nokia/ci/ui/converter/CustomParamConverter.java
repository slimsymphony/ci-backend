package com.nokia.ci.ui.converter;

import com.nokia.ci.ejb.CustomParamEJB;
import com.nokia.ci.ejb.model.CustomParam;
import javax.enterprise.context.RequestScoped;
import javax.faces.convert.FacesConverter;

/**
 * Converter class for custom param entities.
 * 
 * @author vrouvine
 */
@RequestScoped
@FacesConverter("CustomParamConverter")
public class CustomParamConverter extends EntityConverter<CustomParam> {

    public CustomParamConverter() {
        super(CustomParamEJB.class);
    }
}
