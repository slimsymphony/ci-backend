package com.nokia.ci.ejb.model;


import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 10/10/12
 * Time: 9:31 AM
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "PROJECT_GROUP")
public class ProjectGroup extends BaseEntity implements Comparable<ProjectGroup> {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    @Lob
    private String description;
    @Column(name = "CUSTOM_ORDER", nullable=true)
    private Integer order;
    @OneToMany(mappedBy = "projectGroup", cascade = {CascadeType.MERGE,
            CascadeType.DETACH, CascadeType.PERSIST, CascadeType.REFRESH})
    private List<Project> projects = new ArrayList<Project>();

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    @Override
    public int compareTo(ProjectGroup o) {
        if (o==null){
            return -1;
        }
        if (this.getOrder()!=null && o.getOrder()!=null){
            return this.getOrder().compareTo(o.getOrder());
        }else if (this.getOrder()==null && o.getOrder()==null){
            return 0;
        }else if (this.getOrder()==null) {
            return 1;
        }else {
            return -1;
        }
    }
}
