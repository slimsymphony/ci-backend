/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author jajuutin
 */
@Entity
@Table(name = "SYS_CONFIG")
public class SysConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true)
    private String configKey;
    private String configValue;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the configKey
     */
    public String getConfigKey() {
        return configKey;
    }

    /**
     * @param configKey the configKey to set
     */
    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    /**
     * @return the configValue
     */
    public String getConfigValue() {
        return configValue;
    }

    /**
     * @param configValue the configValue to set
     */
    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }
}
