package com.nokia.ci.ui.converter;

import com.nokia.ci.ejb.UserFileEJB;
import com.nokia.ci.ejb.model.UserFile;
import javax.enterprise.context.RequestScoped;
import javax.faces.convert.FacesConverter;

/**
 * Converter for UserFile entities.
 *
 * @author larryang
 */
@RequestScoped
@FacesConverter("UserFileConverter")
public class UserFileConverter extends EntityConverter<UserFile> {

    public UserFileConverter() {
        super(UserFileEJB.class);
    }
}
