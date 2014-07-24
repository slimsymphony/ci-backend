/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.cicontroller;

/**
 *
 * @author jajuutin
 */
public enum CIParam {

    /**
     * Note.
     * 
     * Not all definitions have "CI20" prefix. These definitions are
     * shared with Gerrit and are without prefix on purpose.
     * This enables reusability in Jenkins jobs.
     */
    GERRIT_REFSPEC("GERRIT_REFSPEC"),
    GERRIT_PATCHSET_REVISION("GERRIT_PATCHSET_REVISION"),
    GERRIT_BRANCH("GERRIT_BRANCH"),
    GERRIT_URL("CI20_GERRIT_URL"), 
    GERRIT_PROJECT("GERRIT_PROJECT"),
    GERRIT_CHANGE_OWNER_EMAIL("GERRIT_CHANGE_OWNER_EMAIL"),
    BUILD_ID("CI20_BUILD_ID"),
    JOBS("CI20_JOBS"),
    DISPLAY_NAME("CI20_DISPLAY_NAME"),
    PRODUCT("CI20_PRODUCT"),
    RM_CODE("CI20_RM_CODE"),
    PRODUCT_OVERRIDE("SAIT_PRODUCT_OVERRIDE"),
    TAS_ADDRESS("SAIT_TAS_ADDRESS"),
    TRIGGER("CI20_TRIGGER"),
    MONITOR("CI20_MONITOR"),
    BUILD_GROUP_ID("CI20_BUILD_GROUP_ID"),
    BACKEND_URL("CI20_BACKEND_URL"),
    FETCH_HEAD("FETCH_HEAD"),
    BUILD_URL("CI20_BUILD_URL"),
    NEXTUSER("NEXTUSER"),
    TEST_FILES("TEST_FILES");

    private String stringValue;

    private CIParam(String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public String toString() {
        return stringValue;
    }
}
