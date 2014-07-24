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
public class MemUsageObject {

    private String filename = "";
    private String path = "";
    private int ram;
    private int rom;
    private int diffRam;
    private int diffRom;
    private String area;
    private String component = "";
    private String owner_email;
    private String owner_name;
    private List<MemUsageObject> libraries = new ArrayList<MemUsageObject>();
    private List<MemUsageObject> components = new ArrayList<MemUsageObject>();
    private List<MemUsageObject> objects = new ArrayList<MemUsageObject>();

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getRam() {
        return ram;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    public int getRom() {
        return rom;
    }

    public void setRom(int rom) {
        this.rom = rom;
    }

    public int getDiffRam() {
        return diffRam;
    }

    public void setDiffRam(int diffRam) {
        this.diffRam = diffRam;
    }

    public int getDiffRom() {
        return diffRom;
    }

    public void setDiffRom(int diffRom) {
        this.diffRom = diffRom;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getOwner_email() {
        return owner_email;
    }

    public void setOwner_email(String owner_email) {
        this.owner_email = owner_email;
    }

    public String getOwner_name() {
        return owner_name;
    }

    public void setOwner_name(String owner_name) {
        this.owner_name = owner_name;
    }

    public List<MemUsageObject> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<MemUsageObject> libraries) {
        this.libraries = libraries;
    }

    public List<MemUsageObject> getComponents() {
        return components;
    }

    public void setComponents(List<MemUsageObject> components) {
        this.components = components;
    }

    public List<MemUsageObject> getObjects() {
        return objects;
    }

    public void setObjects(List<MemUsageObject> objects) {
        this.objects = objects;
    }

    public void diffTo(MemUsageObject object) {
        diffRam = ram - object.getRam();
        diffRom = rom - object.getRom();
    }

    public void diffRecursiveTo(MemUsageObject object) {
        if (object == null) {
            return;
        }

        diffTo(object);
        diffMemObjectList(components, object.getComponents());
        diffMemObjectList(libraries, object.getLibraries());
        diffMemObjectList(objects, object.getObjects());
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (filename != null ? filename.hashCode() : 0);
        hash += (path != null ? path.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MemUsageObject)) {
            return false;
        }

        MemUsageObject other = (MemUsageObject) object;

        if (!filename.equals(other.getFilename())) {
            return false;
        }

        if (!path.equals(other.getPath())) {
            return false;
        }

        return true;
    }

    private void diffMemObjectList(List<MemUsageObject> list, List<MemUsageObject> diffList) {
        if (list == null || diffList == null) {
            return;
        }

        List<MemUsageObject> removedObjects = new ArrayList<MemUsageObject>();

        // Find removed objects and run recursive diff
        for (MemUsageObject objDiff : diffList) {
            boolean found = false;
            for (MemUsageObject obj : list) {
                if (objDiff.equals(obj)) {
                    obj.diffRecursiveTo(objDiff);
                    found = true;
                    break;
                }
            }

            if (found == false) {
                objDiff.setFilename(objDiff.getFilename() + " (REMOVED)");
                objDiff.setDiffRam(-objDiff.getRam());
                objDiff.setDiffRom(-objDiff.getRom());
                removedObjects.add(objDiff);
            }
        }

        // Find new objects in this list
        for (MemUsageObject obj : list) {
            boolean found = false;
            for (MemUsageObject diffObj : diffList) {
                if (obj.equals(diffObj)) {
                    found = true;
                    break;
                }
            }

            if (found == false) {
                obj.setFilename(obj.getFilename() + " (NEW)");
                obj.setDiffRam(obj.getRam());
                obj.setDiffRom(obj.getRom());
            }
        }

        list.addAll(0, removedObjects);
    }
}
