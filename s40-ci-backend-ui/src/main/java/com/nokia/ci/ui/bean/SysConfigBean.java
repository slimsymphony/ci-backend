package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.SysConfig;
import com.nokia.ci.ejb.model.SysConfigKey;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
@ViewScoped
public class SysConfigBean extends AbstractUIBaseBean {

    private static Logger log = LoggerFactory.getLogger(SysConfigBean.class);
    @Inject
    private SysConfigEJB sysConfigEJB;
    private SysConfig sysConfig;

    @Override
    protected void init() throws NotFoundException {
        String sysConfigId = getQueryParam("sysConfigId");
        if (sysConfigId == null) {
            sysConfig = new SysConfig();
            return;
        }
        log.debug("Finding SysConfig {} for editing!", sysConfigId);
        sysConfig = sysConfigEJB.read(Long.parseLong(sysConfigId));
    }

    public String save() {
        log.debug("Save triggered!");

        try {
            if (sysConfig.getId() != null) {
                log.debug("Updating existing sysConfig {}", sysConfig);
                sysConfigEJB.update(sysConfig);
            } else {
                log.debug("Saving new sysConfig!");
                sysConfigEJB.create(sysConfig);
            }
            return "sysConfigs?faces-redirect=true";
        } catch (NotFoundException nfe) {
            log.warn("Can not save sysConfig {}! Cause: {}", sysConfig, nfe.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "sysConfig could not be saved!", "");
        }

        return null;
    }

    public SysConfig getSysConfig() {
        return sysConfig;
    }

    public void setSysConfig(SysConfig sysConfig) {
        this.sysConfig = sysConfig;
    }

    public String cancelEdit() {
        return "sysConfigs?faces-redirect=true";
    }

    public SysConfigKey[] getKeys() {
        return SysConfigKey.values();
    }

    public String getSysConfigDesc() {
        if (sysConfig == null || sysConfig.getConfigKey() == null) {
            return "";
        }
        SysConfigKey key = SysConfigKey.valueOf(sysConfig.getConfigKey());
        if (key == null) {
            return "";
        }
        return key.getDesc();
    }
}
