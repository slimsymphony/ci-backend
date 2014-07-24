/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.converter;

import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.model.BuildGroup;
import javax.enterprise.context.RequestScoped;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author hhellgre
 */
@FacesConverter("BuildGroupConverter")
@RequestScoped
public class BuildGroupConverter extends EntityConverter<BuildGroup> {

    public BuildGroupConverter() {
        super(BuildGroupEJB.class);
    }
}
