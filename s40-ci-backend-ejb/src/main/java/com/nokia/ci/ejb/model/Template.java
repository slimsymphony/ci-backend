package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "TEMPLATE")
public class Template extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String description;
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL)
    private List<TemplateVerificationConf> verificationConfs = new ArrayList<TemplateVerificationConf>();
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL)
    private List<TemplateCustomVerification> customVerifications = new ArrayList<TemplateCustomVerification>();    
    @OneToMany(mappedBy = "template", cascade = {})
    private List<Branch> branches = new ArrayList<Branch>();
    
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TemplateVerificationConf> getVerificationConfs() {
        return verificationConfs;
    }

    public void setVerificationConfs(List<TemplateVerificationConf> verificationConfs) {
        this.verificationConfs = verificationConfs;
    }

    public List<Branch> getBranches() {
        return branches;
    }

    public void setBranches(List<Branch> branches) {
        this.branches = branches;
    }

    public List<TemplateCustomVerification> getCustomVerifications() {
        return customVerifications;
    }

    public void setCustomVerifications(List<TemplateCustomVerification> customVerifications) {
        this.customVerifications = customVerifications;
    }
}
