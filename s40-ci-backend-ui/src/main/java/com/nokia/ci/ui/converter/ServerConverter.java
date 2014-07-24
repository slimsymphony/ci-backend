package com.nokia.ci.ui.converter;

import com.nokia.ci.ejb.CIServerEJB;
import com.nokia.ci.ejb.model.CIServer;
import javax.enterprise.context.RequestScoped;
import javax.faces.convert.FacesConverter;

/**
 * Converter for CIServer entities.
 *
 * @author jajuutin
 */
@RequestScoped
@FacesConverter("ServerConverter")
public class ServerConverter extends EntityConverter<CIServer> {

    public ServerConverter() {
        super(CIServerEJB.class);
    }
}
