package com.nokia.ci.ui.converter;

import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.model.SysUser;
import javax.enterprise.context.RequestScoped;
import javax.faces.convert.FacesConverter;

/**
 * Converter for system user entities.
 *
 * @author jajuutin
 */
@RequestScoped
@FacesConverter("SysUserConverter")
public class SysUserConverter extends EntityConverter<SysUser> {

    public SysUserConverter() {
        super(SysUserEJB.class);
    }
}
