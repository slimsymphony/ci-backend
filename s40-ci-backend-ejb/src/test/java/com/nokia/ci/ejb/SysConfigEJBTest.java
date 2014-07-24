/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.SysConfig;
import com.nokia.ci.ejb.model.SysConfigKey;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jajuutin
 */
public class SysConfigEJBTest extends EJBTestBase {

    private SysConfigEJB subject;

    @Override
    @Before
    public void before() {
        super.before();
        subject = new SysConfigEJB();
        subject.em = em;
        subject.cb = cb;
        subject.sysConfigCache = new CacheImpl();
    }

    @Test
    public void getSysConfig() throws NotFoundException {
        // setup
        SysConfig sysConfig = createEntity(SysConfig.class, 1L);
        populateSysConfig(sysConfig);

        TypedQuery mockQuery = createTypedQueryMock(sysConfig);
        mockCriteriaQuery(SysConfig.class, mockQuery, SysConfig.class);

        // run
        SysConfig result = subject.getSysConfig(sysConfig.getConfigKey());

        // verify
        verifySysConfig(sysConfig, result);
    }

    @Test(expected = NotFoundException.class)
    public void getSysConfigNotFound() throws NotFoundException {
        // setup
        TypedQuery mockQuery = createTypedQueryMock(new NoResultException());
        mockCriteriaQuery(SysConfig.class, mockQuery, SysConfig.class);

        // run
        subject.getSysConfig("not found");
    }

    @Test
    public void getSysConfigViaEnum() throws NotFoundException {
        // setup
        SysConfig sysConfig = createEntity(SysConfig.class, 1L);
        populateSysConfig(sysConfig);
        sysConfig.setConfigKey(SysConfigKey.ALLOW_LOGIN.toString());

        TypedQuery mockQuery = createTypedQueryMock(sysConfig);
        mockCriteriaQuery(SysConfig.class, mockQuery, SysConfig.class);

        // run
        SysConfig result = subject.getSysConfig(SysConfigKey.ALLOW_LOGIN);

        // verify
        verifySysConfig(sysConfig, result);
    }

    @Test(expected = NonUniqueResultException.class)
    public void getSysConfigDbIntegrityError() throws NotFoundException {
        // setup
        TypedQuery mockQuery = createTypedQueryMock(new NonUniqueResultException());
        mockCriteriaQuery(SysConfig.class, mockQuery, SysConfig.class);

        // run
        subject.getSysConfig("<irrelevant>");
    }

    @Test
    public void getValueLong() {
        long longValue = 2L;

        // setup
        SysConfig sysConfig = createEntity(SysConfig.class, 1L);
        populateSysConfig(sysConfig);
        sysConfig.setConfigKey(SysConfigKey.ALLOW_LOGIN.toString());
        sysConfig.setConfigValue(Long.toString(longValue));

        TypedQuery mockQuery = createTypedQueryMock(sysConfig);
        mockCriteriaQuery(SysConfig.class, mockQuery, SysConfig.class);

        // run
        long result = subject.getValue(SysConfigKey.ALLOW_LOGIN, 1L);

        // verify
        Assert.assertEquals(longValue, result);
    }

    @Test
    public void getValueLongConfigNotFound() {
        long defaultValue = 2L;

        // setup
        TypedQuery mockQuery = createTypedQueryMock(new NoResultException());
        mockCriteriaQuery(SysConfig.class, mockQuery, SysConfig.class);

        // run
        long result = subject.getValue(SysConfigKey.ALLOW_LOGIN, defaultValue);

        // verify
        Assert.assertEquals(result, defaultValue);
    }

    @Test
    public void getValueLongConfigValueNull() {
        long defaultValue = 2L;

        // setup
        SysConfig sysConfig = createEntity(SysConfig.class, 1L);
        populateSysConfig(sysConfig);
        sysConfig.setConfigKey(SysConfigKey.ALLOW_LOGIN.toString());
        sysConfig.setConfigValue(null);

        TypedQuery mockQuery = createTypedQueryMock(sysConfig);
        mockCriteriaQuery(SysConfig.class, mockQuery, SysConfig.class);

        // run
        long result = subject.getValue(SysConfigKey.ALLOW_LOGIN, defaultValue);

        // verify
        Assert.assertEquals(result, defaultValue);
    }

    @Test
    public void getValueLongConfigValueNotLong() {
        long defaultValue = 2L;

        // setup
        SysConfig sysConfig = createEntity(SysConfig.class, 1L);
        populateSysConfig(sysConfig);
        sysConfig.setConfigKey(SysConfigKey.ALLOW_LOGIN.toString());
        sysConfig.setConfigValue("NON_NUMERICAL_VALUE");

        TypedQuery mockQuery = createTypedQueryMock(sysConfig);
        mockCriteriaQuery(SysConfig.class, mockQuery, SysConfig.class);

        // run
        long result = subject.getValue(SysConfigKey.ALLOW_LOGIN, defaultValue);

        // verify
        Assert.assertEquals(result, defaultValue);
    }
}
