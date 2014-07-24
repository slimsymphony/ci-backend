package com.nokia.ci.ui.converter;

import com.nokia.ci.ejb.SlavePoolEJB;
import com.nokia.ci.ejb.model.SlavePool;
import javax.enterprise.context.RequestScoped;
import javax.faces.convert.FacesConverter;

/**
 * Converter for SlavePool entities.
 *
 * @author jaakkpaa
 */
@RequestScoped
@FacesConverter("SlavePoolConverter")
public class SlavePoolConverter extends EntityConverter<SlavePool> {

    public SlavePoolConverter() {
        super(SlavePoolEJB.class);
    }
}
