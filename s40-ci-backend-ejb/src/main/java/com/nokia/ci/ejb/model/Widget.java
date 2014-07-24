/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 *
 * @author hhellgre
 */
@Entity
@Table(name = "WIDGET")
public class Widget extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NotNull
    private String className;
    @ManyToOne
    private SysUser sysUser;
    @OneToMany(mappedBy = "widget", cascade = CascadeType.ALL)
    private List<WidgetSetting> settings = new ArrayList<WidgetSetting>();
    @NotNull
    private String identifier;
    private String header;
    private Integer columnIndex = 0;
    private Integer itemIndex = 0;
    private Boolean minimized = false;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public SysUser getSysUser() {
        return sysUser;
    }

    public void setSysUser(SysUser sysUser) {
        this.sysUser = sysUser;
    }

    public Integer getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(Integer columnIndex) {
        this.columnIndex = columnIndex;
    }

    public Integer getItemIndex() {
        return itemIndex;
    }

    public void setItemIndex(Integer itemIndex) {
        this.itemIndex = itemIndex;
    }

    public Boolean getMinimized() {
        return minimized;
    }

    public void setMinimized(Boolean minimized) {
        this.minimized = minimized;
    }

    public List<WidgetSetting> getSettings() {
        return settings;
    }

    public void setSettings(List<WidgetSetting> settings) {
        this.settings = settings;
    }
}
