/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.widget;

import com.nokia.ci.ejb.model.Widget;

/**
 *
 * @author hhellgre
 */
public class UserJobsWidget extends BaseJobsWidget {

    @Override
    public void init(Widget w) {
        this.valueExpr = "#{myToolboxBean.userJobs}";
        super.init(w);
    }
}
