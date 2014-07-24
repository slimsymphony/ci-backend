package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Boost;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

@Entity
@Table(name = "CHANGE")
@Indexed(index = "changes")
public class Change extends SecurityEntity implements Comparable<Change> {

    private static final long serialVersionUID = 1L;
    // SUBJECT_MAX_SIZE must be at least 4
    private static final int SUBJECT_MAX_SIZE = 150;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "change_sequence")
    @SequenceGenerator(name = "change_sequence", sequenceName = "CHANGE_SEQ", allocationSize = 10)
    private Long id;
    @Field(name = "changeCommitId")
    @Boost(1.2f)
    @Column(unique = true)
    private String commitId;
    @Field(name = "changeAuthorName")
    private String authorName;
    @Field(name = "changeAuthorEmail")
    private String authorEmail;
    @Field(name = "changeSubject")
    @Boost(1.1f)
    @Analyzer(impl = ClassicAnalyzer.class)
    private String subject;
    @Field(name = "changeUrl")
    private String url;
    @Lob
    @Field(name = "changeMessage")
    @Analyzer(impl = ClassicAnalyzer.class)
    private String message;
    @ManyToMany
    @JoinTable(name = "CHANGE_CHANGE")
    private List<Change> parentChanges = new ArrayList<Change>();
    @ManyToMany(mappedBy = "parentChanges")
    private List<Change> childChanges = new ArrayList<Change>();
    @OneToMany(mappedBy = "change", cascade = CascadeType.ALL)
    @IndexedEmbedded(depth = 1)
    private List<ChangeFile> changeFiles = new ArrayList<ChangeFile>();
    @Temporal(TemporalType.TIMESTAMP)
    private Date commitTime;
    @Enumerated(EnumType.STRING)
    private ChangeStatus status;
    @ManyToMany(mappedBy = "changes")
    @ContainedIn
    private List<BuildGroup> buildGroups = new ArrayList<BuildGroup>();
    private Boolean hasNextUser;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(Date commitTime) {
        this.commitTime = commitTime;
    }

    public ChangeStatus getStatus() {
        return status;
    }

    public void setStatus(ChangeStatus status) {
        this.status = status;
    }

    public List<BuildGroup> getBuildGroups() {
        return buildGroups;
    }

    public void setBuildGroups(List<BuildGroup> buildGroups) {
        this.buildGroups = buildGroups;
    }

    public List<ChangeFile> getChangeFiles() {
        return changeFiles;
    }

    public void setChangeFiles(List<ChangeFile> changeFiles) {
        this.changeFiles = changeFiles;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Change> getParentChanges() {
        return parentChanges;
    }

    public void setParentChanges(List<Change> parentChanges) {
        this.parentChanges = parentChanges;
    }

    public List<Change> getChildChanges() {
        return childChanges;
    }

    public void setChildChanges(List<Change> childChanges) {
        this.childChanges = childChanges;
    }
    
    public Boolean getHasNextUser() {
        return hasNextUser;
    }
    
    public void setHasNextUser(Boolean hasNextUser) {
        this.hasNextUser = hasNextUser;
    }

    @Override
    @PrePersist
    public void prePersist() {
        this.subject = StringUtils.abbreviate(this.subject, SUBJECT_MAX_SIZE);
        super.prePersist();
    }

    @Override
    @PreUpdate
    public void preUpdate() {
        this.subject = StringUtils.abbreviate(this.subject, SUBJECT_MAX_SIZE);
        super.preUpdate();
    }

    @Override
    public int compareTo(Change o) {
        if (o == null) {
            return -1;
        }
        if (this.getCommitTime() != null && o.getCommitTime() != null) {
            return this.getCommitTime().compareTo(o.getCommitTime());
        } else if (this.getCommitTime() == null && o.getCommitTime() == null) {
            return 0;
        } else if (this.getCommitTime() == null) {
            return 1;
        } else {
            return -1;
        }
    }

    public void merge(Change source) {
        // Following members are only set once. This data does not change
        // over time.
        if (this.getAuthorEmail() == null) {
            this.setAuthorName(source.getAuthorName());
        }

        if (this.getAuthorEmail() == null) {
            this.setAuthorEmail(source.getAuthorEmail());
        }

        if (this.getSubject() == null) {
            this.setSubject(source.getSubject());
        }

        if (this.getUrl() == null) {
            this.setUrl(source.getUrl());
        }

        if (this.getChangeFiles().isEmpty()) {
            for (ChangeFile cf : source.getChangeFiles()) {
                this.getChangeFiles().add(cf);
                cf.setChange(this);
            }
        }

        if (this.getCommitTime() == null) {
            this.setCommitTime(source.getCommitTime());
        }
        
        if (this.getHasNextUser() == null) {
            this.setHasNextUser(source.getHasNextUser());
        }

        // Following are always overwritten when new data is available.
        this.setMessage(source.getMessage());
        this.setStatus(source.getStatus());
    }
}
