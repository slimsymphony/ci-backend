package com.nokia.ci.ejb.it;


import com.nokia.ci.ejb.SlaveInstanceEJB;
import com.nokia.ci.ejb.model.SlaveInstance;

import java.util.List;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.ApplyScriptBefore;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration tests for SlaveInstanceEJB class. Tests are running inside
 * container.
 *
 * @author ttyppo
 */
@RunWith(Arquillian.class)
@ApplyScriptBefore(value = {"sequences.sql"})
@UsingDataSet(value = {"slave_instance_dataset.yml"})
public class SlaveInstanceEJBIT {

    private static final Logger log = LoggerFactory.getLogger(SlaveInstanceEJBIT.class);
    @Inject
    private SlaveInstanceEJB slaveInstanceEJB;

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return EJBDeploymentCreator.createEJBDeployment("pom.xml");
    }

    @Test
    public void provideSlaveInstances() {
        String slavePool = "pool_1";
        String slaveLabel = "label_1";
        int requestedAmount = 10;
        String master = "http://localhost:8080";

        List<SlaveInstance> providedSlaveInstances = slaveInstanceEJB.provideSlaveInstances(slavePool, slaveLabel, requestedAmount);
        Assert.assertEquals("Reserved slave instance count does not match!", 2, providedSlaveInstances.size());
    }
}
