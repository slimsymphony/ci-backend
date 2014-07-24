/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.testresults;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author hhellgre
 */
public class NJUnitTestSuite {

    private String name;
    private Map<String, String> properties;
    private List<NJUnitTestCase> testCases;
    private int numTestcases = 0;
    private int numFailures = 0;
    private int numSuccess = 0;
    private int numNA = 0;
    private float successPercent = 0;
    private float failPercent = 0;
    private float NApercent;
    private int diffTestcases;
    private int diffFailures = 0;
    private int diffSuccess = 0;
    private int diffNA = 0;
    private float diffSuccessPercent = 0;
    private float diffFailPercent = 0;
    private float diffNAPercent = 0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public List<String> getPropertyKeys() {
        return new ArrayList<String>(properties.keySet());
    }

    public List<NJUnitTestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<NJUnitTestCase> testCases) {
        this.testCases = testCases;
    }

    public int getNumTestcases() {
        return numTestcases;
    }

    public void setNumTestcases(int numTestcases) {
        this.numTestcases = numTestcases;
    }

    public int getNumFailures() {
        return numFailures;
    }

    public void setNumFailures(int numFailures) {
        this.numFailures = numFailures;
    }

    public int getNumSuccess() {
        return numSuccess;
    }

    public void setNumSuccess(int numSuccess) {
        this.numSuccess = numSuccess;
    }

    public int getDiffTestcases() {
        return diffTestcases;
    }

    public void setDiffTestcases(int diffTestcases) {
        this.diffTestcases = diffTestcases;
    }

    public int getDiffFailures() {
        return diffFailures;
    }

    public void setDiffFailures(int diffFailures) {
        this.diffFailures = diffFailures;
    }

    public int getDiffSuccess() {
        return diffSuccess;
    }

    public void setDiffSuccess(int diffSuccess) {
        this.diffSuccess = diffSuccess;
    }

    public float getSuccessPercent() {
        return successPercent;
    }

    public void setSuccessPercent(float successPercent) {
        this.successPercent = successPercent;
    }

    public float getFailPercent() {
        return failPercent;
    }

    public void setFailPercent(float failPercent) {
        this.failPercent = failPercent;
    }

    public float getDiffSuccessPercent() {
        return diffSuccessPercent;
    }

    public void setDiffSuccessPercent(float diffSuccessPercent) {
        this.diffSuccessPercent = diffSuccessPercent;
    }

    public float getDiffFailPercent() {
        return diffFailPercent;
    }

    public void setDiffFailPercent(float diffFailPercent) {
        this.diffFailPercent = diffFailPercent;
    }

    public int getNumNA() {
        return numNA;
    }

    public void setNumNA(int numNA) {
        this.numNA = numNA;
    }

    public float getNApercent() {
        return NApercent;
    }

    public void setNApercent(float NApercent) {
        this.NApercent = NApercent;
    }

    public int getDiffNA() {
        return diffNA;
    }

    public void setDiffNA(int diffNA) {
        this.diffNA = diffNA;
    }

    public float getDiffNAPercent() {
        return diffNAPercent;
    }

    public void setDiffNAPercent(float diffNAPercent) {
        this.diffNAPercent = diffNAPercent;
    }

    public void diffTo(NJUnitTestSuite diff) {
        if (diff == null) {
            return;
        }

        diffTestcases = diff.getNumTestcases();
        diffFailures = diff.getNumFailures();
        diffSuccess = diff.getNumSuccess();
        diffFailPercent = diff.getFailPercent();
        diffSuccessPercent = diff.getSuccessPercent();
        diffNA = diff.getNumNA();
        diffNAPercent = diff.getNApercent();

        for (NJUnitTestCase test : testCases) {
            for (NJUnitTestCase diffTest : diff.getTestCases()) {
                test.diffTo(diffTest);
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (name != null ? name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof NJUnitTestSuite)) {
            return false;
        }

        NJUnitTestSuite other = (NJUnitTestSuite) object;
        if (name.equals(other.getName())) {
            return true;
        }

        return false;
    }
}
