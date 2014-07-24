package com.nokia.ci.ui.util;

import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ui.jenkins.BuildDetailResolver;
import com.nokia.ci.ui.jenkins.JSONChange;
import com.nokia.ci.ui.model.ExtendedBuildGroup;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 12/10/12 Time: 2:38 PM To change
 * this template use File | Settings | File Templates.
 */
public class ExtendedBuildUtil {

    public static ExtendedBuildGroup createExtendedBuild(BuildGroup buildGroup, List<Change> changes, int socketTimeout, int connectionTimeout) {
        ExtendedBuildGroup extendedBuildGroup = new ExtendedBuildGroup(buildGroup);
        if (changes != null && !changes.isEmpty()) {
            Collections.sort(changes);
            extendedBuildGroup.setAuthors(collectAuthors(changes));
            extendedBuildGroup.setLastChange(changes.get(changes.size() - 1));
        } else {
            extendedBuildGroup.setAuthors(collectAuthors(buildGroup, socketTimeout, connectionTimeout));
        }
        return extendedBuildGroup;
    }

    private static Set<String> collectAuthors(List<Change> buildChanges) {
        Set<String> authors = new HashSet<String>();
        for (Change c : buildChanges) {
            authors.add(c.getAuthorName() + " <" + c.getAuthorEmail() + ">");
        }
        return authors;
    }

    private static Set<String> collectAuthors(BuildGroup buildGroup, int socketTimeout, int connectionTimeout) {
        Set<String> authors = new HashSet<String>();
        if (buildGroup.getUrl() == null) {
            return authors;
        }
        BuildDetailResolver resolver = new BuildDetailResolver(buildGroup.getUrl(), socketTimeout, connectionTimeout);
        List<JSONChange> fetchChanges = resolver.fetchChanges();
        if (fetchChanges != null) {
            for (JSONChange change : fetchChanges) {
                authors.add(change.getAuthor());
            }
        } else {
            authors.add(resolver.getParameterAuthor());
        }
        return authors;
    }
}
