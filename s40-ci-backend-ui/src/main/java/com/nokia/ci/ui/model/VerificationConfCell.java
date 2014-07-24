package com.nokia.ci.ui.model;

import com.nokia.ci.ejb.model.VerificationCardinality;
import com.nokia.ci.ejb.tas.TasDevice;

/**
 *
 * @author vrouvine
 */
public class VerificationConfCell {

    private Long productId;
    private Long verificationId;
    private boolean enabled;
    private VerificationCardinality cardinality;
    private boolean selected;
    private TasDevice device;
    private String rmCode;
    private boolean template;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Long getVerificationId() {
        return verificationId;
    }

    public void setVerificationId(Long verificationId) {
        this.verificationId = verificationId;
    }
    
    public VerificationCardinality getCardinality() {
        return cardinality;
    }
    
    public void setCardinality(VerificationCardinality status) {
        this.cardinality = status;
    }

    public TasDevice getDevice() {
        return device;
    }

    public void setDevice(TasDevice device) {
        this.device = device;
    }

    public String getRmCode() {
        return rmCode;
    }

    public void setRmCode(String rmCode) {
        this.rmCode = rmCode;
    }

    public boolean isTemplate() {
        return template;
    }

    public void setTemplate(boolean template) {
        this.template = template;
    }

}
