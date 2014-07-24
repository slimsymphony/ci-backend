package com.nokia.ci.ui.model;

import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.ProjectGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 10/10/12 Time: 2:28 PM To change
 * this template use File | Settings | File Templates.
 */
public class GroupedProjectModel {

    private Map<ProjectGroupUiLabel, List<Project>> groupedProjects = new HashMap<ProjectGroupUiLabel, List<Project>>();
    private List<ProjectGroupUiLabel> projectGroupUiLabels;
    private ProjectGroupUiLabel unCategorizedDummyGroup = new UnCategorizedGroupUiLabel();

    public GroupedProjectModel(List<Project> projects) {
        groupedProjects = initGroupedProjects(projects);
        projectGroupUiLabels = initSortedGroups(groupedProjects.keySet());
    }

    public List<ProjectGroupUiLabel> getProjectGroupUiLabels() {
        return projectGroupUiLabels;
    }

    public Map<ProjectGroupUiLabel, List<Project>> getGroupedProjects() {
        return groupedProjects;
    }

    public List<Project> getProjectsOfGroup(Long groupID) {
        Iterator<ProjectGroupUiLabel> prjGroups = groupedProjects.keySet().iterator();
        while (prjGroups.hasNext()) {
            ProjectGroupUiLabel prjGroup = prjGroups.next();
            if (prjGroup.getId().equals(groupID)) {
                return groupedProjects.get(prjGroup);
            }
        }
        return null;
    }

    private Map<ProjectGroupUiLabel, List<Project>> initGroupedProjects(List<Project> projects) {
        Map<ProjectGroupUiLabel, List<Project>> groupedProjects = new HashMap<ProjectGroupUiLabel, List<Project>>();
        for (Project project : projects) {
            ProjectGroupUiLabel currentGroup = getProjectGroupUiLabel(project);
            if (groupedProjects.get(currentGroup) == null) {
                groupedProjects.put(currentGroup, new ArrayList<Project>());
            }
            List<Project> currentGroupProjects = groupedProjects.get(currentGroup);
            currentGroupProjects.add(project);
        }
        return groupedProjects;
    }

    private ProjectGroupUiLabel getProjectGroupUiLabel(Project project) {
        ProjectGroup projectGroup = project.getProjectGroup();
        if (projectGroup == null) {
            return unCategorizedDummyGroup;
        } else {
            return createProjectGroupUiLabel(projectGroup);
        }
    }

    private ProjectGroupUiLabel createProjectGroupUiLabel(ProjectGroup projectGroup) {
        ProjectGroupUiLabel projectGroupUiLabel = new ProjectGroupUiLabel();
        projectGroupUiLabel.setId(projectGroup.getId());
        projectGroupUiLabel.setProjectGroup(projectGroup);
        projectGroupUiLabel.setGroupCaption(projectGroup.getName());
        return projectGroupUiLabel;
    }

    private List<ProjectGroupUiLabel> initSortedGroups(Set<ProjectGroupUiLabel> projectGroupUiLabelsSet) {
        List<ProjectGroupUiLabel> projectGroupUiLabels = new ArrayList<ProjectGroupUiLabel>(projectGroupUiLabelsSet);
        Collections.sort(projectGroupUiLabels);
        return projectGroupUiLabels;
    }

    public class ProjectGroupUiLabel implements Comparable<ProjectGroupUiLabel> {

        protected Long id;
        protected String groupCaption;
        private ProjectGroup projectGroup;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getGroupCaption() {
            return groupCaption;
        }

        public void setGroupCaption(String groupCaption) {
            this.groupCaption = groupCaption;
        }

        public ProjectGroup getProjectGroup() {
            return projectGroup;
        }

        public void setProjectGroup(ProjectGroup projectGroup) {
            this.projectGroup = projectGroup;
        }

        @Override
        public int compareTo(ProjectGroupUiLabel other) {
            if (other instanceof UnCategorizedGroupUiLabel) {
                return -1;
            }
            return this.getProjectGroup().compareTo(other.getProjectGroup());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ProjectGroupUiLabel that = (ProjectGroupUiLabel) o;
            if (id != null) {
                if (!id.equals(that.id)) {
                    return false;
                }
            } else if (that.id != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }
    }

    public class UnCategorizedGroupUiLabel extends ProjectGroupUiLabel {

        public UnCategorizedGroupUiLabel() {
            setId(-1l);
            //setGroupCaption("Un-categorized project, not related to any group");
            setGroupCaption("No group");
        }

        @Override
        public int compareTo(ProjectGroupUiLabel other) {
            return 1;
        }
    }
}
