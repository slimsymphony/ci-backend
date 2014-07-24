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
public class UserChangesWidget extends BaseWidget {

    private static final Logger log = LoggerFactory.getLogger(UserChangesWidget.class);

    public UserChangesWidget() {
    }

    public UserChangesWidget(Long id, String header) {
        super(id, header);
        persistentWidget.setIdentifier("user" + id + "Changes");
    }

    @Override
    public void init(Widget w) {
        String id = settings.get("id");
        if (id == null) {
            log.warn("Could not find id setting for UserChangesWidget");
            return;
        }
        this.valueExpr = "#{myToolboxBean.getUserChangesLazyDataModel('" + id + "')}";

        super.init(w);

        Panel rootPanel = super.getPanel();
        Map<String, String> v = new HashMap<String, String>();
        v.put("model", valueExpr);
        addCompositeComponent(rootPanel, "comp", "changeDatatable.xhtml", "t_" + w.getIdentifier(), v);
        render();
    }
}
