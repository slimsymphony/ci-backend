/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.widget;

import com.nokia.ci.ejb.model.Widget;
import java.util.HashMap;
import java.util.Map;
import org.primefaces.component.panel.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
public abstract class BaseBuildsWidget extends BaseWidget {

    private static final Logger log = LoggerFactory.getLogger(BaseBuildsWidget.class);

    public BaseBuildsWidget() {
    }

    public BaseBuildsWidget(Long id, String header) {
        super(id, header);
    }

    @Override
    public void init(Widget w) {
        super.init(w);
        Panel rootPanel = super.getPanel();
        Map<String, String> v = new HashMap<String, String>();
        v.put("model", valueExpr);
        addCompositeComponent(rootPanel, "comp", "buildDatatable.xhtml", "t_" + w.getIdentifier(), v);
        render();
    }
}
