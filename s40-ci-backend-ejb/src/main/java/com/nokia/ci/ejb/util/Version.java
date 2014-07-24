/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.util;

/**
 *
 * @author hhellgre
 */
public class Version {

    private String version;

    /**
     * Constructor
     *
     * @param version created new version from String object
     *
     * @throws IllegalArgumentException in case the version string is not
     * correctly formatted
     */
    public Version(String version) {
        if (version == null) {
            throw new IllegalArgumentException("Version can not be null");
        }
        if (!version.matches("[0-9]+(\\.[0-9]+)*")) {
            throw new IllegalArgumentException("Invalid version format");
        }
        this.version = version;
    }

    /**
     * Compares this version to another
     *
     * @param other Another version object
     * @return true if this version is newer than the other
     */
    public boolean newerThan(Version other) {
        return (compareTo(other) > 0);
    }

    /**
     * Compares this version to another
     *
     * @param other Another version object
     * @return true if this version is older than the other
     */
    public boolean olderThan(Version other) {
        return (compareTo(other) < 0);
    }

    /**
     * Compares this version to another
     *
     * @param other Another version object
     * @return -1 if this one is older, 1 if this is newer
     */
    public int compareTo(Version other) {
        if (other == null) {
            return 1;
        }
        String[] thisParts = version.split("\\.");
        String[] thatParts = other.toString().split("\\.");
        int length = Math.max(thisParts.length, thatParts.length);
        for (int i = 0; i < length; i++) {
            int thisPart = i < thisParts.length
                    ? Integer.parseInt(thisParts[i]) : 0;
            int thatPart = i < thatParts.length
                    ? Integer.parseInt(thatParts[i]) : 0;
            if (thisPart < thatPart) {
                return -1;
            }
            if (thisPart > thatPart) {
                return 1;
            }
        }
        return 0;
    }

    /**
     *
     * @param other Another version object
     * @return true if this version matches the other
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (this.getClass() != other.getClass()) {
            return false;
        }
        return this.compareTo((Version) other) == 0;
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (version != null ? version.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return version;
    }
}
