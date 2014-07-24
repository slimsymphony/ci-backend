package com.nokia.ci.ui.model;

import com.nokia.ci.ejb.model.AbstractCustomParam;
import com.nokia.ci.ejb.model.AbstractCustomVerification;
import com.nokia.ci.ejb.model.CustomVerificationParam;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author vrouvine
 */
public class VerificationConfRow {

    public static final String DEFAULT_PARAM_VALUE = "--Default--";
    public static final String EMPTY_PARAM_VALUE = "--Empty--";
    private String rowLabel;
    private boolean custom;
    private boolean indevice;
    private boolean template;
    private AbstractCustomVerification customVerification;
    private List<VerificationConfCell> cells;

    public VerificationConfRow() {
        this.cells = new ArrayList<VerificationConfCell>();
    }

    public List<VerificationConfCell> getCells() {
        return cells;
    }

    public void setCells(List<VerificationConfCell> cells) {
        this.cells = cells;
    }

    public String getRowLabel() {
        return rowLabel;
    }

    public void setRowLabel(String rowLabel) {
        this.rowLabel = rowLabel;
    }

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    public AbstractCustomVerification getCustomVerification() {
        return customVerification;
    }

    public void setCustomVerification(AbstractCustomVerification customVerification) {
        this.customVerification = customVerification;
    }

    public String getCustomParameterString() {
        if (customVerification == null || customVerification.getAbstractCustomParams() == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        ListIterator<? extends AbstractCustomParam> listIterator = customVerification.getAbstractCustomParams().listIterator();
        while (listIterator.hasNext()) {
            AbstractCustomParam param = listIterator.next();
            if (DEFAULT_PARAM_VALUE.equals(param.getParamValue()) || EMPTY_PARAM_VALUE.equals(param.getParamValue())) {
                continue;
            }
            if (listIterator.previousIndex() > 0 && sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(param.getCustomParam().getDisplayName()).append("=").append(param.getParamValue());
        }
        return sb.toString();
    }

    public boolean isIndevice() {
        return indevice;
    }

    public void setIndevice(boolean indevice) {
        this.indevice = indevice;
    }

    public boolean isTemplate() {
        return template;
    }

    public void setTemplate(boolean template) {
        this.template = template;
    }
}
