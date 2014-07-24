package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ui.bean.AbstractUIBaseBean;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import org.primefaces.model.DashboardColumn;
import org.primefaces.model.DashboardModel;
import org.primefaces.model.DefaultDashboardColumn;
import org.primefaces.model.DefaultDashboardModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author larryang
 */
@ViewScoped
public abstract class MetricsDashboardBeanBase extends AbstractUIBaseBean implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(MetricsDashboardBeanBase.class);
    private DashboardModel model;
    private int placementPointer;

    @Override
    public void init() throws BackendAppException {
        placementPointer = 0;
        model = new DefaultDashboardModel();
        DashboardColumn leftColumn = new DefaultDashboardColumn();
        DashboardColumn rightColumn = new DefaultDashboardColumn();
        model.addColumn(leftColumn);
        model.addColumn(rightColumn);
    }

    public DashboardModel getModel() {
        return model;
    }

    public void addWidget(String widget) {
        if (model == null) {
            placementPointer = 0;
            model = new DefaultDashboardModel();
            DashboardColumn leftColumn = new DefaultDashboardColumn();
            DashboardColumn rightColumn = new DefaultDashboardColumn();
            model.addColumn(leftColumn);
            model.addColumn(rightColumn);
        }

        if (placementPointer % 2 == 0) {
            model.getColumn(0).addWidget(widget);
        } else {
            model.getColumn(1).addWidget(widget);
        }

        placementPointer++;
    }
}
