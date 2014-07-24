package com.nokia.ci.integration.rest;

import com.nokia.ci.client.model.IncidentView;
import com.nokia.ci.client.rest.IncidentResource;
import com.nokia.ci.ejb.model.IncidentType;
import com.nokia.ci.integration.CITestBase;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 4/10/13
 * Time: 2:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class IncidentResourceIT extends CITestBase {

    private static Logger log = LoggerFactory.getLogger(IncidentResourceIT.class);

    private static IncidentResource proxy;

    @BeforeClass
    public static void setUpClass() {
        proxy = ProxyFactory.create(IncidentResource.class, API_BASE_URL);
    }

    @Test
    public void createIncident() throws Exception {
        Date time = new Date();
        IncidentType type = IncidentType.DELIVERY_CHAIN;
        String desc = "incident test desc";
        Date checkTime = new Date();
        String checkUser = "user";
        IncidentView i = new IncidentView();
        i.setTime(time);
        i.setType(type);
        i.setDescription(desc);
        i.setCheckTime(checkTime);
        i.setCheckUser(checkUser);
        ClientResponse r = (ClientResponse) proxy.createIncident(i);
        Assert.assertNotNull(r.getLocation());
        String resultLocation = r.getLocation().getHref();
        Assert.assertEquals(ClientResponse.Status.CREATED.getStatusCode(), r.getStatus());
        String resultId = parseIdFromLocationUrl(resultLocation);
        r = (ClientResponse) proxy.getIncident(Long.valueOf(resultId));
        IncidentView created = (IncidentView) r.getEntity(IncidentView.class);
        Assert.assertEquals(time, created.getTime());
        Assert.assertEquals(type, created.getType());
        Assert.assertEquals(desc, created.getDescription());
        Assert.assertEquals(checkTime, created.getCheckTime());
        Assert.assertEquals(checkUser, created.getCheckUser());
    }

    @Test
    public void getIncident() throws Exception {
        String id = DatasetTool.getDatasetValue(
                dataset, DatasetTool.INCIDENT_TABLE_NAME, 0, "ID");
        String desc = DatasetTool.getDatasetValue(
                dataset, DatasetTool.INCIDENT_TABLE_NAME, 0, "DESCRIPTION");
        String time = DatasetTool.getDatasetValue(
                        dataset, DatasetTool.INCIDENT_TABLE_NAME, 0, "TIME");
        String type = DatasetTool.getDatasetValue(
                dataset, DatasetTool.INCIDENT_TABLE_NAME, 0, "TYPE");
        String checkTime = DatasetTool.getDatasetValue(
                        dataset, DatasetTool.INCIDENT_TABLE_NAME, 0, "CHECKTIME");
        String checkUser = DatasetTool.getDatasetValue(
                        dataset, DatasetTool.INCIDENT_TABLE_NAME, 0, "CHECKUSER");


        log.info("Incident id: " + id);
        ClientResponse response = (ClientResponse) proxy.getIncident(Long.valueOf(id));
        Assert.assertEquals(ClientResponse.Status.OK.getStatusCode(), response.getStatus());
        IncidentView incident = (IncidentView) response.getEntity(IncidentView.class);
        Assert.assertNotNull(incident);
        Assert.assertEquals(Long.valueOf(id), incident.getId());
        Assert.assertEquals(type, incident.getType().toString());
        Date incidentTime = incident.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String incidentTimeAsString = formatter.format(incidentTime);
        Assert.assertEquals(time, incidentTimeAsString);
        Assert.assertEquals(desc, incident.getDescription());
        Date incidentCheckTime = incident.getCheckTime();
        String incidentCheckTimeAsString = formatter.format(incidentCheckTime);
        Assert.assertEquals(checkTime, incidentCheckTimeAsString);
        Assert.assertEquals(checkUser, incident.getCheckUser());


    }

}
