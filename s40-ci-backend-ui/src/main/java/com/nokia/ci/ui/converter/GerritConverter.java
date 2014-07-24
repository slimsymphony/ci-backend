package com.nokia.ci.ui.converter;

import com.nokia.ci.ejb.GerritEJB;
import com.nokia.ci.ejb.model.Gerrit;
import javax.enterprise.context.RequestScoped;
import javax.faces.convert.FacesConverter;

/**
 * Converter for gerrit entities.
 *
 * @author jajuutin
 */
@RequestScoped
@FacesConverter("GerritConverter")
public class GerritConverter extends EntityConverter<Gerrit> {

    public GerritConverter() {
        super(GerritEJB.class);
    }
}
