package com.nokia.ci.ui.converter;

import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.model.Job;
import javax.enterprise.context.RequestScoped;
import javax.faces.convert.FacesConverter;

/**
 * Converter for branch entities.
 *
 * @author jajuutin
 */
@RequestScoped
@FacesConverter("JobConverter")
public class JobConverter extends EntityConverter<Job> {

    public JobConverter() {
        super(JobEJB.class);
    }
}
