package com.nokia.ci.ejb.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entity class for customized parameter values.
 *
 * @author vrouvine
 */
@Entity
@Table(name = "CUSTOM_PARAM_VALUE")
public class CustomParamValue extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String paramValue;
    @ManyToOne
    private CustomParam customParam;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public CustomParam getCustomParam() {
        return customParam;
    }

    public void setCustomParam(CustomParam customParam) {
        this.customParam = customParam;
    }
}
