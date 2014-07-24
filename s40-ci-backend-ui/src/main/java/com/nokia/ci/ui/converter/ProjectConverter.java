package com.nokia.ci.ui.converter;

import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.model.Project;
import javax.enterprise.context.RequestScoped;
import javax.faces.convert.FacesConverter;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 10/1/12 Time: 2:29 PM To change
 * this template use File | Settings | File Templates.
 */
@RequestScoped
@FacesConverter("ProjectConverter")
public class ProjectConverter extends EntityConverter<Project> {

    public ProjectConverter() {
        super(ProjectEJB.class);
    }
}
