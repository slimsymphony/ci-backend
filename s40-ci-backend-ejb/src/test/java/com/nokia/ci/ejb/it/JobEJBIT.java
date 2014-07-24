package com.nokia.ci.ejb.it;

import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.model.JobTriggerType;
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
 * Integration tests for JobEJB class. Tests are running inside container.
 *
 * @author vrouvine
 */
@RunWith(Arquillian.class)
@ApplyScriptBefore(value = {"sequences.sql"})
@UsingDataSet(value = {"jobs_dataset.yml"})
public class JobEJBIT {

    private static final Logger log = LoggerFactory.getLogger(JobEJBIT.class);
    @Inject
    private JobEJB jobEJB;

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return EJBDeploymentCreator.createEJBDeployment("pom.xml");
    }

    @Test
    public void getJobs() {
        Assert.assertEquals("Jobs count does not match!", 5, jobEJB.readAll().size());
    }

    @Test
    public void getJobsByTriggerType() {
        Assert.assertEquals("Automatic job count does not match!", 1, jobEJB.getJobsByTriggerType(JobTriggerType.AUTOMATIC).size());
        Assert.assertEquals("Poll job count does not match!", 1, jobEJB.getJobsByTriggerType(JobTriggerType.POLL).size());
        Assert.assertEquals("Manual job count does not match!", 2, jobEJB.getJobsByTriggerType(JobTriggerType.MANUAL).size());
        Assert.assertEquals("Scheduled job count does not match!", 1, jobEJB.getJobsByTriggerType(JobTriggerType.SCHEDULE).size());
    }
}
