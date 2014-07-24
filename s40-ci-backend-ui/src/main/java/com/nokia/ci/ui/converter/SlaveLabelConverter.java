package com.nokia.ci.ui.converter;

import com.nokia.ci.ejb.SlaveLabelEJB;
import com.nokia.ci.ejb.model.SlaveLabel;
import javax.enterprise.context.RequestScoped;
import javax.faces.convert.FacesConverter;

/**
 * Converter for SlaveLabel entities.
 *
 * @author jaakkpaa
 */
@RequestScoped
@FacesConverter("SlaveLabelConverter")
public class SlaveLabelConverter extends EntityConverter<SlaveLabel> {

    public SlaveLabelConverter() {
        super(SlaveLabelEJB.class);
    }
}
