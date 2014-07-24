package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Entity class of job custom verification.
 *
 * @author jajuutin
 */
@Entity
@Table(name = "TEMPLATE_CUSTOM_VERIFICATION")
public class TemplateCustomVerification extends AbstractCustomVerification {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Lob
    private String description;
    @ManyToOne
    private Template template;
    @ManyToOne
    private Verification verification;
    @OneToMany(mappedBy = "customVerification", cascade = CascadeType.ALL)
    private List<TemplateCustomVerificationConf> customVerificationConfs = new ArrayList<TemplateCustomVerificationConf>();
    @OneToMany(mappedBy = "customVerification", cascade = CascadeType.ALL)
    private List<TemplateCustomVerificationParam> customVerificationParams = new ArrayList<TemplateCustomVerificationParam>();

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    @Override
    public Verification getVerification() {
        return verification;
    }

    @Override
    public void setVerification(Verification verification) {
        this.verification = verification;
    }

    public List<TemplateCustomVerificationConf> getCustomVerificationConfs() {
        return customVerificationConfs;
    }

    public void setCustomVerificationConfs(List<TemplateCustomVerificationConf> customVerificationConfs) {
        this.customVerificationConfs = customVerificationConfs;
    }

    public List<TemplateCustomVerificationParam> getCustomVerificationParams() {
        return customVerificationParams;
    }

    public void setCustomVerificationParams(List<TemplateCustomVerificationParam> customVerificationParams) {
        this.customVerificationParams = customVerificationParams;
    }

    @Override
    public List<? extends AbstractCustomParam> getAbstractCustomParams() {
        return customVerificationParams;
    }
}
