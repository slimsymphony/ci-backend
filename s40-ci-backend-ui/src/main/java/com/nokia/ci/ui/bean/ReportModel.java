package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.model.ReportAction;
import com.nokia.ci.ejb.reportaction.ReportActionStatus;
import com.nokia.ci.ejb.reportaction.ReportActionTitle;

/**
 * Reporting model class for UI.
 *
 * @author vrouvine
 */
public class ReportModel<T extends ReportAction> {

    private boolean enabled;
    private ReportActionStatus status;
    private T action;
    private ReportActionTitle title;

    public ReportModel(boolean enabled, ReportActionStatus status, T action, ReportActionTitle title) {
        this.enabled = enabled;
        this.status = status;
        this.action = action;
        this.title = title;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ReportActionStatus getStatus() {
        return status;
    }

    public void setStatus(ReportActionStatus status) {
        this.status = status;
    }

    public T getAction() {
        return action;
    }

    public void setAction(T action) {
        this.action = action;
    }

    public ReportActionTitle getTitle() {
        return title;
    }

    public void setTitle(ReportActionTitle title) {
        this.title = title;
    }
}
