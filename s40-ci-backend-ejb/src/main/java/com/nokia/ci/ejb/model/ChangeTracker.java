package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "CHANGE_TRACKER")
public class ChangeTracker extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "changetracker_sequence")
    @SequenceGenerator(name = "changetracker_sequence", sequenceName = "CHANGE_TRACKER_SEQ", allocationSize = 10)
    private Long id;
    @Temporal(TemporalType.TIMESTAMP)
    private Date scvStart;
    @Temporal(TemporalType.TIMESTAMP)
    private Date scvEnd;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dbvStart;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dbvEnd;
    @Temporal(TemporalType.TIMESTAMP)
    private Date masterStart;
    @Temporal(TemporalType.TIMESTAMP)
    private Date masterEnd;
    @Temporal(TemporalType.TIMESTAMP)
    private Date released;
    @ManyToMany(mappedBy="changeTrackers")
    private List<Project> projects = new ArrayList<Project>();
    private String commitId;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the scvStart
     */
    public Date getScvStart() {
        return scvStart;
    }

    /**
     * @param scvStart the scvStart to set
     */
    public void setScvStart(Date scvStart) {
        this.scvStart = scvStart;
    }

    /**
     * @return the scvEnd
     */
    public Date getScvEnd() {
        return scvEnd;
    }

    /**
     * @param scvEnd the scvEnd to set
     */
    public void setScvEnd(Date scvEnd) {
        this.scvEnd = scvEnd;
    }

    /**
     * @return the dbvStart
     */
    public Date getDbvStart() {
        return dbvStart;
    }

    /**
     * @param dbvStart the dbvStart to set
     */
    public void setDbvStart(Date dbvStart) {
        this.dbvStart = dbvStart;
    }

    /**
     * @return the dbvEnd
     */
    public Date getDbvEnd() {
        return dbvEnd;
    }

    /**
     * @param dbvEnd the dbvEnd to set
     */
    public void setDbvEnd(Date dbvEnd) {
        this.dbvEnd = dbvEnd;
    }

    /**
     * @return the masterStart
     */
    public Date getMasterStart() {
        return masterStart;
    }

    /**
     * @param masterStart the masterStart to set
     */
    public void setMasterStart(Date masterStart) {
        this.masterStart = masterStart;
    }

    /**
     * @return the masterEnd
     */
    public Date getMasterEnd() {
        return masterEnd;
    }

    /**
     * @param masterEnd the masterEnd to set
     */
    public void setMasterEnd(Date masterEnd) {
        this.masterEnd = masterEnd;
    }

    /**
     * @return the released
     */
    public Date getReleased() {
        return released;
    }

    /**
     * @param released the released to set
     */
    public void setReleased(Date released) {
        this.released = released;
    }

    /**
     * @return the commitId
     */
    public String getCommitId() {
        return commitId;
    }

    /**
     * @param commitId the commitId to set
     */
    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    /**
     * @return the projects
     */
    public List<Project> getProjects() {
        return projects;
    }

    /**
     * @param projects the projects to set
     */
    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }
}
