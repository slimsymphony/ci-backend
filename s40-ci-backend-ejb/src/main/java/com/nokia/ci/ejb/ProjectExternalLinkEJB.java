package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.ProjectExternalLink;
import java.io.Serializable;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@Stateless
@LocalBean
public class ProjectExternalLinkEJB extends CrudFunctionality<ProjectExternalLink> implements Serializable {
    
    private static Logger log = LoggerFactory.getLogger(ProjectExternalLinkEJB.class);
    
    public ProjectExternalLinkEJB() {
        super(ProjectExternalLink.class);
    }
}
