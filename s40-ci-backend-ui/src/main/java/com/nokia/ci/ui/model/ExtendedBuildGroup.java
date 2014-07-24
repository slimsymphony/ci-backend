package com.nokia.ci.ui.model;

import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.Change;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author miikka
 */
public class ExtendedBuildGroup {

    private BuildGroup buildGroup = null;
    private Set<String> authors = new HashSet<String>();
    private String authorsString = "";
    private static final String SEPARATOR = ";\n";
    private Change lastChange = null;

    public ExtendedBuildGroup(BuildGroup buildGroup) {
        this.buildGroup = buildGroup;
    }

    public Set<String> getAuthors() {
        return authors;
    }

    public String getAuthorsString() {
        return authorsString;
    }

    public void addAuthor(String author) {
        this.authors.add(author);
        createAuthorsString();
    }

    public void setAuthors(Set<String> authors) {
        this.authors = authors;
        createAuthorsString();
    }

    private void createAuthorsString() {
        if (authors == null || authors.isEmpty()) {
            return;
        }
        StringBuilder authorsSB = new StringBuilder();
        for (String author : authors) {
            if (!author.isEmpty()) {
                authorsSB.append(author);
                authorsSB.append(SEPARATOR);
            }
        }
        int lastSeparator = authorsSB.lastIndexOf(SEPARATOR);
        if (lastSeparator > 0) {
            authorsSB.replace(lastSeparator, authorsSB.length(), "");
        }
        authorsString = authorsSB.toString();
    }

    private ExtendedBuildGroup() {
    }

    public BuildGroup getBuildGroup() {
        return buildGroup;
    }

    public Change getLastChange() {
        return lastChange;
    }

    public void setLastChange(Change lastChange) {
        this.lastChange = lastChange;
    }
}
