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
public class WarningObject {

    private int newWarnings = 0;
    private int fixedWarnings = 0;
    private String path;
    private String file;
    private String lineNumber;
    private String warningID;
    private String warningMsg;
    private List<WarningObject> warnings = new ArrayList<WarningObject>();

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getWarningID() {
        return warningID;
    }

    public void setWarningID(String warningID) {
        this.warningID = warningID;
    }

    public String getWarningMsg() {
        return warningMsg;
    }

    public void setWarningMsg(String warningMsg) {
        this.warningMsg = warningMsg;
    }

    public List<WarningObject> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<WarningObject> warnings) {
        this.warnings = warnings;
    }

    public int getNewWarnings() {
        return newWarnings;
    }

    public void setNewWarnings(int newWarnings) {
        this.newWarnings = newWarnings;
    }

    public int getFixedWarnings() {
        return fixedWarnings;
    }

    public void setFixedWarnings(int fixedWarnings) {
        this.fixedWarnings = fixedWarnings;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (path != null ? path.hashCode() : 0);
        if (path == null) {
            hash += (file != null ? file.hashCode() : 0);
            hash += (lineNumber != null ? lineNumber.hashCode() : 0);
            hash += (warningID != null ? warningID.hashCode() : 0);
            hash += (warningMsg != null ? warningMsg.hashCode() : 0);
        }
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof WarningObject)) {
            return false;
        }

        WarningObject other = (WarningObject) object;

        if (path == null || path.isEmpty()) {
            if (!file.equals(other.getFile())) {
                return false;
            }

            if (!lineNumber.equals(other.getLineNumber())) {
                return false;
            }

            if (!warningID.equals(other.getWarningID())) {
                return false;
            }

            if (!warningMsg.equals(other.getWarningMsg())) {
                return false;
            }

            return true;
        }

        if (!path.equals(other.getPath())) {
            return false;
        }

        return true;
    }

    public void diffRecursiveTo(WarningObject object, WarningObject root) {
        if (object == null) {
            return;
        }
        List<WarningObject> diffList = object.getWarnings();

        if (diffList == null || diffList.isEmpty()) {
            return;
        }

        List<WarningObject> removedObjects = new ArrayList<WarningObject>();

        // Find removed objects and run recursive diff
        for (WarningObject objDiff : diffList) {
            boolean found = false;
            for (WarningObject obj : warnings) {
                if (objDiff.equals(obj)) {
                    obj.diffRecursiveTo(objDiff, root);
                    found = true;
                    break;
                }
            }

            if (found == false) {
                root.setFixedWarnings(root.getFixedWarnings() + 1);
                objDiff.setFile(objDiff.getFile() + " (FIXED)");
                removedObjects.add(objDiff);
            }
        }

        // Find new objects in this list
        for (WarningObject obj : warnings) {
            boolean found = false;
            for (WarningObject diffObj : diffList) {
                if (obj.equals(diffObj)) {
                    found = true;
                    break;
                }
            }

            if (found == false) {
                root.setNewWarnings(root.getNewWarnings() + 1);
                obj.setFile(obj.getFile() + " (NEW)");
            }
        }

        warnings.addAll(0, removedObjects);
    }
}
