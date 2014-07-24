/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.widget;

import com.nokia.ci.ejb.metrics.MetricsLevel;
import com.nokia.ci.ejb.model.Widget;
import com.nokia.ci.ui.bean.metrics.MetricsBeanBase;
import java.util.HashMap;
import java.util.Map;
import org.primefaces.component.panel.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author larryang
 */
public class MetricsChartWidget extends BaseWidget {

    private static final Logger log = LoggerFactory.getLogger(MetricsChartWidget.class);

    public MetricsChartWidget() {
    }

    public MetricsChartWidget(Long id, MetricsBeanBase metricsBean, String header) {
        
        super(id, header);

        persistentWidget.setIdentifier(metricsBean.getMetricsLevel().toString() + id + metricsBean.getRenderDiv());
        addSettingItem("metricsLevel", metricsBean.getMetricsLevel().toString());
        addSettingItem("jsfComponent", metricsBean.getJsfComponent());
    }
    


    @Override
    public void init(Widget w) {
        String id = settings.get("id");
        String metricsLevel = settings.get("metricsLevel");
        String jsfComponent = settings.get("jsfComponent");
        
        if (id == null && !metricsLevel.equals(MetricsLevel.SYSTEM.toString())){
            log.warn("Could not find id setting for MetricsChartWidget " + w.getIdentifier());
            return;
        }
        super.init(w);

        Panel rootPanel = super.getPanel();
        Map<String, String> v = new HashMap<String, String>();
        if (metricsLevel.equals(MetricsLevel.VERIFICATION.toString())){
            v.put("verificationId", id);
        }else if (metricsLevel.equals(MetricsLevel.BUILD.toString())){
            v.put("buildId", id);
        }
        addCompositeComponent(rootPanel, "comp", jsfComponent, "c_" + w.getIdentifier(), v);
        render();
    }
}
