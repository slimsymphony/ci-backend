package com.nokia.ci.ui.converter;

import com.nokia.ci.ejb.TemplateEJB;
import com.nokia.ci.ejb.model.Template;
import javax.enterprise.context.RequestScoped;
import javax.faces.convert.FacesConverter;

/**
 * Converter for template entities.
 *
 * @author jajuutin
 */
@RequestScoped
@FacesConverter("TemplateConverter")
public class TemplateConverter extends EntityConverter<Template> {

    public TemplateConverter() {
        super(TemplateEJB.class);
    }
}
