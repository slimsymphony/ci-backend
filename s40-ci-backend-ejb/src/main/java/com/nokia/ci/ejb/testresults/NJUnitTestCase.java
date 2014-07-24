/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.testresults;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hhellgre
 */
public class NJUnitTestCase {

    private String name;
    private String classname;
    private String time;
    private String relativePath;
    private String diffRelativePath;
    private List<NJUnitTestFailure> failures;
    private List<NJUnitTestFailure> diffFailures;
    private List<NJUnitTestNA> notAllowed;
    private List<NJUnitTestNA> diffNotAllowed;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<NJUnitTestFailure> getFailures() {
        return failures;
    }

    public void setFailures(List<NJUnitTestFailure> failures) {
        this.failures = failures;
    }

    public List<NJUnitTestFailure> getDiffFailures() {
        return diffFailures;
    }

    public void setDiffFailures(List<NJUnitTestFailure> diffFailures) {
        this.diffFailures = diffFailures;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getDiffRelativePath() {
        return diffRelativePath;
    }

    public void setDiffRelativePath(String diffRelativePath) {
        this.diffRelativePath = diffRelativePath;
    }

    public List<NJUnitTestNA> getNotAllowed() {
        return notAllowed;
    }

    public void setNotAllowed(List<NJUnitTestNA> notAllowed) {
        this.notAllowed = notAllowed;
    }

    public List<NJUnitTestNA> getDiffNotAllowed() {
        return diffNotAllowed;
    }

    public void setDiffNotAllowed(List<NJUnitTestNA> diffNotAllowed) {
        this.diffNotAllowed = diffNotAllowed;
    }

    public void diffTo(NJUnitTestCase diff) {
        if (!diff.getClassname().equals(classname)
                || !diff.getName().equals(name)) {
            return;
        }

        diffRelativePath = diff.getRelativePath();
        diffFailures = new ArrayList<NJUnitTestFailure>();
        if (diff.getFailures() != null) {
            diffFailures.addAll(diff.getFailures());
        }

        diffNotAllowed = new ArrayList<NJUnitTestNA>();
        if (diff.getNotAllowed() != null) {
            diffNotAllowed.addAll(diff.getNotAllowed());
        }
    }
}
