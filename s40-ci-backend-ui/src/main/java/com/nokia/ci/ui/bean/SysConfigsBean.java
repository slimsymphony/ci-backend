package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.index.SearchIndexOptimizator;
import com.nokia.ci.ejb.model.SysConfig;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.reaper.BuildGroupReaper;
import com.nokia.ci.ejb.reaper.SysUserReaper;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managed bean class for system configs.
 *
 * @author jajuutin
 */
@Named
public class SysConfigsBean extends DataFilterBean<SysConfig> {

    private static Logger log = LoggerFactory.getLogger(SysConfigsBean.class);
    @Inject
    private SysConfigEJB sysConfigEJB;
    @Inject
    private SearchIndexOptimizator searchIndexOptimizator;
    @Inject
    private BuildGroupReaper buildGroupReaper;
    @Inject
    private SysUserReaper sysUserReaper;
    private List<SysConfig> sysConfigs;

    @Override
    protected void init() {
        initSysConfigs();
    }

    public void delete(SysConfig sysConfig) {
        log.info("Deleting sysConfig {}", sysConfig);
        try {
            String key = sysConfig.getConfigKey();
            sysConfigEJB.delete(sysConfig);
            sysConfigs.remove(sysConfig);
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "SysConfig " + key + " was deleted.");
        } catch (NotFoundException ex) {
            log.warn("Deleting SysConfig {} failed! Cause: {}", sysConfig, ex.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Delete SysConfig failed!", "Selected SysConfig could not be deleted!");
        }
    }

    private void initSysConfigs() {
        sysConfigs = sysConfigEJB.readAll();
    }

    public List<SysConfig> getSysConfigs() {
        return sysConfigs;
    }

    public String getSysConfigDesc(SysConfig sysConfig) {
        SysConfigKey key = SysConfigKey.valueOf(sysConfig.getConfigKey());
        return key.getDesc();
    }

    public void optimizeSearchIndex() {
        searchIndexOptimizator.fire();
        addMessage(FacesMessage.SEVERITY_INFO,
                "Operation successful.", "Search indexes are being optimized");
    }

    public void reapBuildGroups() {
        buildGroupReaper.runReaper();
        addMessage(FacesMessage.SEVERITY_INFO,
                "Operation successful.", "Build group reaper started");
    }

    public void reapSysUsers() {
        sysUserReaper.fire();
        addMessage(FacesMessage.SEVERITY_INFO,
                "Operation succesful.", "Sys user reaper started");
    }
}
