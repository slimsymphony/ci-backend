/*
 * Component entity
 */
package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author larryang
 */
@Entity
@Table(name = "COMPONENT")
public class Component extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private BuildGroup buildGroup;
    @OneToMany(mappedBy = "component", cascade = {})
    private List<MemConsumption> memConsumptions = new ArrayList<MemConsumption>();
    @OneToMany(mappedBy = "component", cascade = {})
    private List<TestCaseStat> testCaseStats = new ArrayList<TestCaseStat>();
    @OneToMany(mappedBy = "component", cascade = {})
    private List<TestCoverage> testCoverages = new ArrayList<TestCoverage>();

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public BuildGroup getBuildGroup() {
        return buildGroup;
    }

    public void setBuildGroup(BuildGroup buildGroup) {
        this.buildGroup = buildGroup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MemConsumption> getMemConsumptions() {
        return memConsumptions;
    }

    public void setMemConsumptions(List<MemConsumption> memConsumptions) {
        this.memConsumptions = memConsumptions;
    }

    public List<TestCaseStat> getTestCaseStats() {
        return testCaseStats;
    }

    public void setTestCaseStats(List<TestCaseStat> testCaseStats) {
        this.testCaseStats = testCaseStats;
    }

    public List<TestCoverage> getTestCoverages() {
        return testCoverages;
    }

    public void setTestCoverages(List<TestCoverage> testCoverages) {
        this.testCoverages = testCoverages;
    }
}
