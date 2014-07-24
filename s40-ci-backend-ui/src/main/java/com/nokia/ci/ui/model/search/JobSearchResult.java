/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.model.search;

import com.nokia.ci.ejb.model.BaseEntity;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ui.bean.annotation.SearchResult;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author hhellgre
 */
@SearchResult(type = Job.class, prefix = "verification")
public class JobSearchResult extends BaseSearchResult {

    private static final String urlBase = "/secure/pages/verificationDetails.xhtml?verificationId=";

    public JobSearchResult(BaseEntity entity) {
        super(entity);
        Job job = (Job) entity;
        url = urlBase + job.getId();
        header = "Verification " + job.getDisplayName();
        description = job.getName();
        if (job.getBranch() != null && StringUtils.isEmpty(job.getBranch().getDisplayName()) == false) {
            description += " / Branch: " + job.getBranch().getDisplayName();
        }

        if (StringUtils.isEmpty(job.getContactPerson()) == false) {
            description += " / Contact person: " + job.getContactPerson();
        }
    }
}
