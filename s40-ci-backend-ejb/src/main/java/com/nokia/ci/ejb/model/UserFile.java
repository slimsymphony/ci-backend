package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entity class of User File.
 *
 * @author larryang
 */
@Entity
@Table(name = "USER_FILE")
public class UserFile extends SecurityEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    @Column(nullable = false, unique = true)
    private String uuid;
    private String filePath;
    private Long fileSize;
    private String mimeType;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private SysUser owner;
    @Enumerated(EnumType.STRING)
    private OwnershipScope ownershipScope;
    @Enumerated(EnumType.STRING)
    private AccessScope accessScope;
    @Enumerated(EnumType.STRING)
    private FileType fileType = FileType.GENERAL;
    @ManyToMany(mappedBy = "userFiles")
    private List<JobVerificationConf> jobVerificationConfs = new ArrayList<JobVerificationConf>();
    @ManyToMany(mappedBy = "userFiles")
    private List<CustomVerificationConf> customVerificationConfs = new ArrayList<CustomVerificationConf>();

    public List<CustomVerificationConf> getCustomVerificationConfs() {
        return customVerificationConfs;
    }

    public void setCustomVerificationConfs(List<CustomVerificationConf> customVerificationConfs) {
        this.customVerificationConfs = customVerificationConfs;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public List<JobVerificationConf> getJobVerificationConfs() {
        return jobVerificationConfs;
    }

    public void setJobVerificationConfs(List<JobVerificationConf> jobVerificationConfs) {
        this.jobVerificationConfs = jobVerificationConfs;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SysUser getOwner() {
        return owner;
    }

    public void setOwner(SysUser owner) {
        this.owner = owner;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public AccessScope getAccessScope() {
        return accessScope;
    }

    public void setAccessScope(AccessScope accessScope) {
        this.accessScope = accessScope;
    }

    public OwnershipScope getOwnershipScope() {
        return ownershipScope;
    }

    public void setOwnershipScope(OwnershipScope ownershipScope) {
        this.ownershipScope = ownershipScope;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        String ret = super.toString();
        ret += " (" + filePath + uuid + ")";
        return ret;
    }
}
