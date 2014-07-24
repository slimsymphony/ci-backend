package com.nokia.ci.ui.jenkins;

import java.util.List;
import org.primefaces.json.JSONObject;

/**
 *
 * @author vrouvine
 */
public class JSONChange {
    
    private String author = "";
    private JSONObject changeItem;
    private List<JSONObject> paths;

    public JSONChange(JSONObject changeItem) {
        this.changeItem = changeItem;
    }

    public JSONObject getChangeItem() {
        return changeItem;
    }

    public void setChangeItem(JSONObject changeItem) {
        this.changeItem = changeItem;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<JSONObject> getPaths() {
        return paths;
    }

    public void setPaths(List<JSONObject> paths) {
        this.paths = paths;
    }
}
