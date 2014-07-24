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
public class JobBuildsWidget extends BaseBuildsWidget {

    private static final Logger log = LoggerFactory.getLogger(JobBuildsWidget.class);

    public JobBuildsWidget() {
    }

    public JobBuildsWidget(Long id, String header) {
        super(id, header);
        persistentWidget.setIdentifier("job" + id + "Builds");
    }

    @Override
    public void init(Widget w) {
        String id = settings.get("id");
        if (id == null) {
            log.warn("Could not find id setting for JobBuildsWidget");
            return;
        }
        this.valueExpr = "#{myToolboxBean.getVerificationBuildsLazyDataModel('" + id + "')}";
        super.init(w);
    }
}
