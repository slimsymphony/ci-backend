package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.NotFoundException;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author larryang
 */
@Named
public class SysMetricsDashboardBean extends MetricsDashboardBeanBase {

    private static final Logger log = LoggerFactory.getLogger(SysMetricsDashboardBean.class);

    @Override
    public void init() throws BackendAppException {
        super.init();
        addWidget("commitTrendPanel");
        addWidget("subveriTrendPanel");
        addWidget("hangtimeTrendPanel");
        addWidget("slaveTrendPanel");
    }
}
