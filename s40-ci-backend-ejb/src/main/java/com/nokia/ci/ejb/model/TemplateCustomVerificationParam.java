package com.nokia.ci.ejb.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entity class of custom verification parameter.
 *
 * @author jajuutin
 */
@Entity
@Table(name = "TEMPLATE_CUST_VER_PARAM")
public class TemplateCustomVerificationParam extends AbstractCustomParam {
    
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String paramValue;
    @ManyToOne
    private CustomParam customParam;
    @ManyToOne
    private TemplateCustomVerification customVerification;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getParamValue() {
        return paramValue;
    }

    @Override
    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    @Override
    public CustomParam getCustomParam() {
        return customParam;
    }

    @Override
    public void setCustomParam(CustomParam customParam) {
        this.customParam = customParam;
    }

    public TemplateCustomVerification getCustomVerification() {
        return customVerification;
    }

    public void setCustomVerification(TemplateCustomVerification customVerification) {
        this.customVerification = customVerification;
    }
}
