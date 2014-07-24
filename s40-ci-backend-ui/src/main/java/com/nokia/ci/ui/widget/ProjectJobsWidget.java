/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.widget;

import com.nokia.ci.ejb.model.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
public class ProjectJobsWidget extends BaseJobsWidget {

    private static final Logger log = LoggerFactory.getLogger(ProjectJobsWidget.class);

    public ProjectJobsWidget() {
    }

    public ProjectJobsWidget(Long id, String header) {
        super(id, header);
        persistentWidget.setIdentifier("project" + id + "Jobs");
    }

    @Override
    public void init(Widget w) {
        String id = settings.get("id");
        if (id == null) {
            log.warn("Could not find id setting for ProjectJobsWidget");
            return;
        }
        this.valueExpr = "#{myToolboxBean.getProjectJobs('" + id + "')}";
        super.init(w);
    }
}
