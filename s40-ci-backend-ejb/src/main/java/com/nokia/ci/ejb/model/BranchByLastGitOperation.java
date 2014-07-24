/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.model;

import java.util.Comparator;
import java.util.Date;

/**
 *
 * @author jajuutin
 *
 * This comparator is used to arrange by lastGitOperationStarted. Oldest
 * dates first. Null before anything else. If both objects have
 * lastGitOperationStarter set as null, then arrange by random.
 *
 * This logic is used by GitTimer to balance git load.
 */
public class BranchByLastGitOperation implements Comparator<Branch> {

    static int BEFORE = -1;
    static int EQUAL = 0;
    static int AFTER = 1;

    @Override
    public int compare(Branch b1, Branch b2) {
        Date time1 = b1.getLastGitOperationStarted();
        Date time2 = b2.getLastGitOperationStarted();

        if (time1 == null && time2 != null) {
            return BEFORE;
        }

        if (time1 != null && time2 == null) {
            return AFTER;
        }

        if (time1 == null && time2 == null) {
            return EQUAL;
        }

        if (time1.before(time2)) {
            return BEFORE;
        }

        if (time1.after(time2)) {
            return AFTER;
        }

        if (time1.getTime() == time2.getTime()) {
            return EQUAL;
        }

        // Fallback to EQUAL.
        return EQUAL;
    }
}
