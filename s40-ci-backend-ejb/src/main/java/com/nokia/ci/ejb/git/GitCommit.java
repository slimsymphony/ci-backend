package com.nokia.ci.ejb.git;

import java.util.Date;
import java.util.List;

/**
 *
 * @author miikka
 */
public class GitCommit {

    private String id;
    private String author;
    private Date date;
    private String message;
    private List<GitChange> changes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<GitChange> getChanges() {
        return changes;
    }

    public void setChanges(List<GitChange> changes) {
        this.changes = changes;
    }
}
