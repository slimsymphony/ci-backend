/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.model;

/**
 *
 * @author hhellgre
 */
public enum ChangeFileType {

    ADDED, MODIFIED, REMOVED, RENAMED, COPIED, REWRITE;

    /**
     * Returns exactly matching enum constant value. Whitespaces are trimmed
     * from given string value.
     *
     * @param value Given string to match enum constants
     * @return Matching enum constant value. {@code Null} is returned if no
     * matching enum contant found.
     */
    public static ChangeFileType fromString(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        try {
            return valueOf(trimmed);
        } catch (IllegalArgumentException iae) {
        }
        return null;
    }
}
