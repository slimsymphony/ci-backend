package com.nokia.ci.ejb.git;

/**
 *
 * @author miikka
 */
public class GitChange {

    public static enum Type {

        ADD, DELETE, MODIFY, RENAMED
    }
    private String file;
    private Type type;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
