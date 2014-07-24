/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.apache.solr.analysis.LowerCaseFilterFactory;
import org.apache.solr.analysis.PathHierarchyTokenizerFactory;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;

/**
 *
 * @author hhellgre
 */
@Entity
@Table(name = "CHANGE_FILE")
@AnalyzerDef(name = "fileAnalyzer",
        tokenizer =
        @TokenizerDef(factory = PathHierarchyTokenizerFactory.class),
        filters = {
    @TokenFilterDef(factory = LowerCaseFilterFactory.class)
})
public class ChangeFile extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "change_file_sequence")
    @SequenceGenerator(name = "change_file_sequence", sequenceName = "CHANGE_FILE_SEQ", allocationSize = 50)
    private Long id;
    @Column(length = 2000)
    @Field(name = "changeFilePath")
    @Analyzer(definition = "fileAnalyzer")
    private String filePath;
    @Enumerated(EnumType.STRING)
    private ChangeFileType fileType;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    @ContainedIn
    private Change change;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Change getChange() {
        return change;
    }

    public void setChange(Change change) {
        this.change = change;
    }

    public ChangeFileType getFileType() {
        return fileType;
    }

    public void setFileType(ChangeFileType fileType) {
        this.fileType = fileType;
    }
}
