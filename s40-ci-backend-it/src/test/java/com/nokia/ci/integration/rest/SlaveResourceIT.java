package com.nokia.ci.integration.rest;

import com.nokia.ci.client.model.SlaveView;
import com.nokia.ci.client.rest.SlaveResource;
import com.nokia.ci.integration.CITestBase;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.servlet.http.HttpServletResponse;
import org.dbunit.dataset.DataSetException;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.util.GenericType;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP API tests for slave resource.
 *
 * @author vrouvine
 */
public class SlaveResourceIT extends CITestBase {

    private static Logger log = LoggerFactory.getLogger(SlaveResourceIT.class);
    private static SlaveResource proxy;

    public SlaveResourceIT() {
        super("load_balancer.xml");
    }

    @BeforeClass
    public static void setUpClass() {
        proxy = ProxyFactory.create(SlaveResource.class, API_BASE_URL);
    }

    @Test
    public void getAvailableSlaves() throws DataSetException {
        final String type = "available";
        // Pool 1 label linux
        String slavePool = (String) dataset.getTable("S40CICORE.SLAVE_POOL").getValue(0, "NAME");
        final String slaveLabel = (String) dataset.getTable("S40CICORE.SLAVE_LABEL").getValue(1, "NAME");
        ClientResponse response = (ClientResponse) proxy.action(slavePool, type, slaveLabel, null, null, null);

        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        GenericType genericType = (new GenericType<Boolean[]>() {
        });
        Boolean[] bools = (Boolean[]) response.getEntity(genericType);
        Assert.assertTrue("There should be available instances!", bools[0]);

        // Pool 2 null labels
        slavePool = (String) dataset.getTable("S40CICORE.SLAVE_POOL").getValue(1, "NAME");
        response = (ClientResponse) proxy.action(slavePool, type, null, null, null, null);

        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        bools = (Boolean[]) response.getEntity(genericType);
        Assert.assertTrue("There should be available instances!", bools[0]);
        
        // Pool 3 linux labels No free
        slavePool = (String) dataset.getTable("S40CICORE.SLAVE_POOL").getValue(2, "NAME");
        response = (ClientResponse) proxy.action(slavePool, type, slaveLabel, null, null, null);

        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        bools = (Boolean[]) response.getEntity(genericType);
        Assert.assertFalse("There should not be available instances!", bools[0]);
    }

    @Test
    public void getReserved() throws DataSetException {
        final String type = "status-reserved";
        GenericType genericType = (new GenericType<Map<String, Integer>>() {
        });
        // Pool 1, Label linux, Machine 1 and 2 
        String slavePool = (String) dataset.getTable("S40CICORE.SLAVE_POOL").getValue(0, "NAME");
        String slaveLabel = (String) dataset.getTable("S40CICORE.SLAVE_LABEL").getValue(1, "NAME");
        
        ClientResponse response = (ClientResponse) proxy.action(slavePool, type, slaveLabel, null, null, null);
        
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Map<String, Integer> reserved = (Map<String, Integer>) response.getEntity(genericType);
        Assert.assertEquals("Reserved count does not match!", Integer.valueOf(2), reserved.get(getMachineUrl(1)));
        Assert.assertEquals("Reserved count does not match!", Integer.valueOf(1), reserved.get(getMachineUrl(2)));
        
        // Pool 1, Label win, Machine 1
        slaveLabel = (String) dataset.getTable("S40CICORE.SLAVE_LABEL").getValue(0, "NAME");
        response = (ClientResponse) proxy.action(slavePool, type, slaveLabel, null, null, null);
        
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        reserved = (Map<String, Integer>) response.getEntity(genericType);
        Assert.assertEquals("Reserved count does not match!", Integer.valueOf(1), reserved.get(getMachineUrl(1)));
        
        // Pool 2, Label win, Machine 3
        slavePool = (String) dataset.getTable("S40CICORE.SLAVE_POOL").getValue(1, "NAME");
        slaveLabel = (String) dataset.getTable("S40CICORE.SLAVE_LABEL").getValue(0, "NAME");
        response = (ClientResponse) proxy.action(slavePool, type, slaveLabel, null, null, null);
        
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        reserved = (Map<String, Integer>) response.getEntity(genericType);
        Assert.assertEquals("Reserved count does not match!", Integer.valueOf(2), reserved.get(getMachineUrl(3)));
        
        // Pool 2, no label, Machine 5
        response = (ClientResponse) proxy.action(slavePool, type, null, null, null, null);
        
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        reserved = (Map<String, Integer>) response.getEntity(genericType);
        Assert.assertNull("There should not be reserved instances!", reserved.get(getMachineUrl(5)));
    }
    
    @Test
    public void releaseSlave() throws Exception {
        final String type = "release";
        final Long slaveInstanceId = 1L;
        final String slavePool = (String) dataset.getTable("S40CICORE.SLAVE_POOL").getValue(1, "NAME");
        String currentMaster = (String) connection.createQueryTable("temp", "SELECT CURRENTMASTER FROM S40CICORE.SLAVE_INSTANCE WHERE ID=" + slaveInstanceId)
                .getValue(0, "CURRENTMASTER");
        Assert.assertNotNull("Current master should not be null!", currentMaster);

        ClientResponse response = (ClientResponse) proxy.action(slavePool, type, null, null, null, slaveInstanceId + "_machine4_8080");
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        Boolean released = (Boolean) response.getEntity(Boolean.class);
        Assert.assertTrue("Response should be true", released);

        currentMaster = (String) connection.createQueryTable("temp", "SELECT CURRENTMASTER FROM S40CICORE.SLAVE_INSTANCE WHERE ID=" + slaveInstanceId)
                .getValue(0, "CURRENTMASTER");
        Assert.assertNull("Current master should be null!", currentMaster);
    }

    @Test
    public void releaseSlaveNotFound() throws Exception {
        final String type = "release";
        final String slavePool = (String) dataset.getTable("S40CICORE.SLAVE_POOL").getValue(1, "NAME");

        ClientResponse response = (ClientResponse) proxy.action(slavePool, type, null, null, null, Long.MAX_VALUE + "_machine4_8080");
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }

    @Test
    public void provideSlaveInstances() throws Exception {
        final String type = "provision";
        // Pool 1, Label linux 
        String slavePool = (String) dataset.getTable("S40CICORE.SLAVE_POOL").getValue(0, "NAME");
        String slaveLabel = (String) dataset.getTable("S40CICORE.SLAVE_LABEL").getValue(1, "NAME");
        Long requestedSlaves = 4L;
        String master = "Master";

        ClientResponse response = (ClientResponse) proxy.action(slavePool, type, slaveLabel, requestedSlaves, master, null);
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        GenericType genericType = (new GenericType<List<SlaveView>>() {
        });
        List<SlaveView> slaves = (List<SlaveView>) response.getEntity(genericType);
        Assert.assertNotNull(slaves);
        Assert.assertEquals("Slave size should match!", requestedSlaves.intValue(), slaves.size());
        for (SlaveView slave : slaves) {
            Assert.assertEquals("Current master should match!", master, slave.getCurrentMaster());
        }
    }

    @Test
    public void provideSlaveInstancesMultiThreded() throws Exception {
        final String type = "provision";
        // Pool 1
        String slavePool1 = (String) dataset.getTable("S40CICORE.SLAVE_POOL").getValue(0, "NAME");
        // Pool 2
        String slavePool2 = (String) dataset.getTable("S40CICORE.SLAVE_POOL").getValue(1, "NAME");
        Long requestedSlaves = 10L;
        String master1 = "Master_1";
        String master2 = "Master_2";

        int threadCount = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        List<Future<List<SlaveView>>> futures = new ArrayList<Future<List<SlaveView>>>();
        for (int i = 0; i < threadCount; i++) {
            String slavePool = slavePool1;
            String master = master1;
            if (i % 2 == 0) {
                // Use pool 2 and master 2
                slavePool = slavePool2;
                master = master2;
            }
            ProvisionTask provisionTask = new ProvisionTask(slavePool, type, null, requestedSlaves, master);
            Future<List<SlaveView>> future = pool.submit(provisionTask);
            futures.add(future);
        }

        // Analyze results
        Set<String> names = new HashSet<String>();
        for (Future<List<SlaveView>> future : futures) {
            List<SlaveView> slaveViews = future.get();
            for (SlaveView slaveView : slaveViews) {
                int sizeBefore = names.size();
                names.add(slaveView.getName());
                Assert.assertFalse("Found dublicate provision for " + slaveView.getName() + "! Already added provisions " + names, sizeBefore == names.size());
            }
        }
    }

    private String getMachineUrl(int id) throws DataSetException {
        return (String) dataset.getTable("S40CICORE.SLAVE_MACHINE").getValue(id-1, "URL");
    }

    private class ProvisionTask implements Callable<List<SlaveView>> {

        private String slavePool;
        private String type;
        private String slaveLabel;
        private Long requestedSlaves;
        private String master;
        private SlaveResource resource;

        public ProvisionTask(String slavePool, String type, String slaveLabel, Long requestedSlaves, String master) {
            this.slavePool = slavePool;
            this.type = type;
            this.slaveLabel = slaveLabel;
            this.requestedSlaves = requestedSlaves;
            this.master = master;
        }

        @Override
        public List<SlaveView> call() {
            log.info("Start provision...");
            try {
                resource = ProxyFactory.create(SlaveResource.class, API_BASE_URL);
                ClientResponse response = (ClientResponse) resource.action(slavePool, type, slaveLabel, requestedSlaves, master, null);
                Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
                GenericType genericType = (new GenericType<List<SlaveView>>() {
                });
                List<SlaveView> slaves = (List<SlaveView>) response.getEntity(genericType);
                log.info("Stopped provision with {} slaves", slaves.size());
                return slaves;
            } catch (Exception ex) {
                log.error("Provision failed!", ex);
            }
            return null;
        }
    }
}
