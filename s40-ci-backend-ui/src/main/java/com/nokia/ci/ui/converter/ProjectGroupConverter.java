package com.nokia.ci.ui.converter;

import com.nokia.ci.ejb.ProjectGroupEJB;
import com.nokia.ci.ejb.model.ProjectGroup;
import javax.enterprise.context.RequestScoped;
import javax.faces.convert.FacesConverter;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 10/10/12 Time: 10:23 AM To
 * change this template use File | Settings | File Templates.
 */
@RequestScoped
@FacesConverter("ProjectGroupConverter")
public class ProjectGroupConverter extends EntityConverter<ProjectGroup> {

    public ProjectGroupConverter() {
        super(ProjectGroupEJB.class);
    }
}
