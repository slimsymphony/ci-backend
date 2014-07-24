package com.nokia.ci.ejb.model;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Entity class for customized parameters.
 *
 * @author vrouvine
 */
@Entity
@Table(name = "CUSTOM_PARAM")
public class CustomParam extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String displayName;
    private String paramKey;
    @OneToMany(mappedBy = "customParam", cascade = CascadeType.ALL)
    private List<CustomParamValue> customParamValues;
    @ManyToOne
    private Verification verification;
    @OneToMany(mappedBy = "customParam")
    private List<CustomVerificationParam> customVerificationParams;
    @OneToMany(mappedBy = "customParam")
    private List<TemplateCustomVerificationParam> templateCustomVerificationParams;
    
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getParamKey() {
        return paramKey;
    }

    public void setParamKey(String paramKey) {
        this.paramKey = paramKey;
    }

    public List<CustomParamValue> getCustomParamValues() {
        return customParamValues;
    }

    public void setCustomParamValues(List<CustomParamValue> customParamValues) {
        this.customParamValues = customParamValues;
    }

    public Verification getVerification() {
        return verification;
    }

    public void setVerification(Verification verification) {
        this.verification = verification;
    }

    public List<CustomVerificationParam> getCustomVerificationParams() {
        return customVerificationParams;
    }

    public void setCustomVerificationParams(List<CustomVerificationParam> customVerificationParams) {
        this.customVerificationParams = customVerificationParams;
    }

    public List<TemplateCustomVerificationParam> getTemplateCustomVerificationParams() {
        return templateCustomVerificationParams;
    }

    public void setTemplateCustomVerificationParams(List<TemplateCustomVerificationParam> templateCustomVerificationParams) {
        this.templateCustomVerificationParams = templateCustomVerificationParams;
    }
}
