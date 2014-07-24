/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.SysConfig;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.model.SysConfig_;
import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 */
@Stateless
@LocalBean
public class SysConfigEJB extends CrudFunctionality<SysConfig> {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SysConfigEJB.class);
    @Resource(lookup = "java:jboss/infinispan/cache/ci20/session-cache")
    Cache<String, SysConfig> sysConfigCache;

    public SysConfigEJB() {
        super(SysConfig.class);
    }

    public SysConfig getSysConfig(SysConfigKey configKey) throws NotFoundException {
        return getSysConfig(configKey.toString());
    }

    public boolean isLoginAllowed() {
        return getValue(SysConfigKey.ALLOW_LOGIN, false);
    }

    public boolean isNextUsersAllowed() {
        return getValue(SysConfigKey.ALLOW_NEXT_USERS, false);
    }

    public boolean configExists(SysConfigKey configKey) {
        String value = getValue(configKey);
        if (value == null) {
            return false;
        }
        return true;
    }

    public boolean getValue(SysConfigKey configKey, boolean defaultValue) {
        String value = getValue(configKey);
        if (value == null) {
            logNotFound(configKey, Boolean.toString(defaultValue));
            return defaultValue;
        }

        return Boolean.valueOf(value);
    }

    public int getValue(SysConfigKey configKey, int defaultValue) {
        String value = getValue(configKey);
        if (value == null) {
            logNotFound(configKey, Integer.toString(defaultValue));
            return defaultValue;
        }

        int result = defaultValue;
        try {
            result = Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            logNumberFormatException(configKey, Integer.toString(defaultValue), ex);
        }

        return result;
    }

    public long getValue(SysConfigKey configKey, long defaultValue) {
        String value = getValue(configKey);
        if (value == null) {
            logNotFound(configKey, Long.toString(defaultValue));
            return defaultValue;
        }

        long result = defaultValue;
        try {
            result = Long.valueOf(value);
        } catch (NumberFormatException ex) {
            logNumberFormatException(configKey, Long.toString(defaultValue), ex);
        }

        return result;
    }

    public String getValue(SysConfigKey configKey, String defaultValue) {
        String value = getValue(configKey);
        if (value == null) {
            logNotFound(configKey, defaultValue);
            return defaultValue;
        }

        return value;
    }

    public String getValueNoLog(SysConfigKey configKey, String defaultValue) {
        String value = getValue(configKey);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public SysConfig getSysConfig(String configKey) throws NotFoundException {
        log.debug("Finding SysConfig where confKey={}", configKey);

        if (sysConfigCache.containsKey(configKey)) {
            log.debug("Found SysConfig from cache");
            return sysConfigCache.get(configKey);
        }

        CriteriaQuery<SysConfig> criteria = cb.createQuery(SysConfig.class);
        Root<SysConfig> root = criteria.from(SysConfig.class);
        criteria.where(cb.equal(root.get(SysConfig_.configKey), configKey));

        SysConfig sysConfig = null;

        try {
            sysConfig = em.createQuery(criteria).getSingleResult();
        } catch (NoResultException e) {
            throw new NotFoundException("System configuration not found with key: " + configKey, e);
        } catch (NonUniqueResultException nure) {
            log.error("DB integrity problem detected, duplicate sysConfig key {} found", configKey);
            throw nure;
        } catch (Exception ex) {
            log.error("Error occured during sysConfig read from database: ", ex);
        }

        log.debug("Found SysConfig from database");
        return sysConfig;
    }

    @Override
    public void create(SysConfig config) {
        super.create(config);
        em.detach(config);
        sysConfigCache.put(config.getConfigKey(), config);
    }

    @Override
    public SysConfig update(SysConfig config) throws NotFoundException {
        SysConfig ret = super.update(config);

        em.flush();
        em.detach(ret);
        sysConfigCache.put(config.getConfigKey(), ret);
        return ret;
    }

    @Override
    public void delete(SysConfig config) throws NotFoundException {
        if (sysConfigCache.containsKey(config.getConfigKey())) {
            sysConfigCache.remove(config.getConfigKey());
        }
        super.delete(config);
    }

    private String getValue(SysConfigKey configKey) {
        String value = null;
        try {
            SysConfig sc = getSysConfig(configKey);
            value = sc.getConfigValue();
        } catch (NotFoundException ex) {
        }
        return value;
    }

    private void logNotFound(SysConfigKey sysConfigKey, String defaultValue) {
        StringBuilder sb = new StringBuilder();
        sb.append("System configuration not found for key: ").append(sysConfigKey.toString());
        sb.append(". Using default value: ").append(defaultValue);
        log.warn(sb.toString());
    }

    private void logNumberFormatException(SysConfigKey sysConfigKey, String defaultValue, NumberFormatException ex) {
        StringBuilder sb = new StringBuilder();
        sb.append("System configuration value not valid for key: ").append(sysConfigKey.toString());
        sb.append(". Cause: ").append(ex.getMessage());
        sb.append(". Using default value: ").append(defaultValue);
        log.warn(sb.toString());
    }
}
