/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.model;

/**
 *
 * @author hhellgre
 */
public enum VerificationCardinality {

    MANDATORY, OPTIONAL;

    public BuildStatus combine(BuildStatus buildStatus) {
        if (this == VerificationCardinality.MANDATORY
                && buildStatus == BuildStatus.UNSTABLE) {
            return BuildStatus.FAILURE;
        } else if (this == VerificationCardinality.OPTIONAL
                && buildStatus != BuildStatus.SUCCESS) {
            return BuildStatus.UNSTABLE;
        }
        return buildStatus;
    }
}
