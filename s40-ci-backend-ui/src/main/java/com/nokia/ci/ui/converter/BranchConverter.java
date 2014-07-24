package com.nokia.ci.ui.converter;

import com.nokia.ci.ejb.BranchEJB;
import com.nokia.ci.ejb.model.Branch;
import javax.enterprise.context.RequestScoped;
import javax.faces.convert.FacesConverter;

/**
 * Converter for branch entities.
 *
 * @author jajuutin
 */
@FacesConverter("BranchConverter")
@RequestScoped
public class BranchConverter extends EntityConverter<Branch> {

    public BranchConverter() {
        super(BranchEJB.class);
    }
}
