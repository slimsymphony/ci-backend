/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.model;

/**
 *
 * @author larryang
 */
public class ClassificationStatus {
    
    private boolean classifiable = true;
    private boolean classified = true;
    private boolean classifiedOk = true;
    
    public ClassificationStatus(boolean inClassifiable, boolean inClassified, boolean inClassifiedOk){
        classifiable = inClassifiable;
        classified = inClassified;
        classifiedOk = inClassifiedOk;
    }

    public boolean isClassifiable() {
        return classifiable;
    }

    public void setClassifiable(boolean classifable) {
        this.classifiable = classifable;
    }

    public boolean isClassified() {
        return classified;
    }

    public void setClassified(boolean classified) {
        this.classified = classified;
    }

    public boolean isClassifiedOk() {
        return classifiedOk;
    }

    public void setClassifiedOk(boolean classifiedOk) {
        this.classifiedOk = classifiedOk;
    }
}
