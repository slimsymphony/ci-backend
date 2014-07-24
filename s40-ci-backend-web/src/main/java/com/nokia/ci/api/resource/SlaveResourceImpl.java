package com.nokia.ci.api.resource;

import com.google.gson.Gson;
import com.nokia.ci.client.model.SlaveView;
import com.nokia.ci.client.rest.SlaveResource;
import com.nokia.ci.ejb.CIServerEJB;
import com.nokia.ci.ejb.SlaveInstanceEJB;
import com.nokia.ci.ejb.SlavePoolEJB;
import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.event.LoadBalancerStatEvent;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.incident.IncidentEventHandlerEJB;
import com.nokia.ci.ejb.incident.LoadBalancerIncidentHandlerEJB;
import com.nokia.ci.ejb.model.SlaveInstance;
import com.nokia.ci.ejb.model.SlavePool;
import com.nokia.ci.ejb.model.SysConfigKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jaakkpaa
 *
 */
@Named
@RequestScoped
public class SlaveResourceImpl implements SlaveResource {

    private static Logger log = LoggerFactory.getLogger(SlaveResourceImpl.class);
    @Inject
    SlaveInstanceEJB slaveInstanceEJB;
    @Inject
    SlavePoolEJB slavePoolEJB;
    @Inject
    CIServerEJB ciServerEJB;
    @Inject
    SysUserEJB sysUserEJB;
    @Inject
    LoadBalancerIncidentHandlerEJB loadBalancerIncidentHandlerEJB;
    @Inject
    @LoadBalancerStatEvent
    private Event<Long> loadBalancerStatEvents;
    
    static Gson gson = new Gson();

    /**
     * The main method called in request.
     */
    @Override
    public Response action(String slavePool,
            String type,
            String slaveLabel,
            Long slaveInstances,
            String master,
            String release_id) {
        log.info("SlaveResourceImpl.action( " + slavePool + "," + type + "," + slaveLabel + "," + slaveInstances + "," + master + "," + release_id + ")");

        if (type == null) {
            return (Response.status(Response.Status.NOT_FOUND).header("X-LoadBalancer", "True").build());
        }

        // Change empty or "null" slaveLabel to null
        if (slaveLabel != null && (slaveLabel.isEmpty() || slaveLabel.equals("null"))) {
            slaveLabel = null;
        }

        // Different response types
        if (type.equals("available")) {
            return (availableSlaveInstances(slavePool, slaveLabel, master));
        } else if (type.equals("provision") && slaveInstances >= 0 && master != null) {
            return (provideSlaveInstances(slavePool, slaveLabel, slaveInstances, master));
        } else if (type.equals("release") && release_id != null) {
            return (releaseSlaveInstance(release_id));
        } else if (type.equals("status-reserved")) {
            return (reservedSlaveInstances(slavePool, slaveLabel));
        }

        StringBuilder result = new StringBuilder();
        result.append("Uncaught action. Pool:").append(slavePool);
        result.append(" type:").append(type);
        result.append(" labels:").append(slaveLabel);
        result.append(" executors:").append(slaveInstances);
        result.append(" master:").append(master);
        result.append(" release_id:").append(release_id);

        log.info(result.toString());
        return (Response.status(Response.Status.NOT_FOUND).header("X-LoadBalancer", "True").build());
    }

    /**
     * Get the status for available slaves
     */
    private Response availableSlaveInstances(String slavePool, String slaveLabel, String master) {
        log.info("SlaveResourceImpl.available( " + slavePool + "," + slaveLabel + "," + master + ")");
        int available = slaveInstanceEJB.getAvailableSlaveInstances(slavePool, slaveLabel).size();
        Boolean[] resultBool = {available > 0};
        return (Response
                .ok(this.gson.toJson(resultBool))
                .header("X-LoadBalancer", "True")
                .build());
    }

    /**
     * Get the status for reserved slaves
     */
    private Response reservedSlaveInstances(String slavePool, String slaveLabel) {
        log.info("SlaveResourceImpl.reservedSlaveInstances( " + slavePool + "," + slaveLabel + ")");
        List<SlaveInstance> reservedSlaveInstances = slaveInstanceEJB.getReservedSlaveInstances(slavePool, slaveLabel);

        Map<String, Integer> slaveInstanceMap = new HashMap<String, Integer>();
        for (SlaveInstance slaveInstance : reservedSlaveInstances) {
            Integer slaveInstances = slaveInstanceMap.get(slaveInstance.getSlaveMachine().getUrl());
            if (slaveInstances == null) {
                slaveInstances = 0;
            }
            slaveInstanceMap.put(slaveInstance.getSlaveMachine().getUrl(), ++slaveInstances);
        }

        return (Response
                .ok(this.gson.toJson(slaveInstanceMap))
                .header("X-LoadBalancer", "True")
                .build());
    }

    /**
     * Provide requested amount of instances on certain master having specified
     * slave pool and labels
     */
    private Response provideSlaveInstances(String slavePool, String slaveLabel, Long slaveInstances, String master) {
        long startTime = System.currentTimeMillis();
        log.info("Provision with ( " + slavePool + "," + slaveLabel + "," + slaveInstances + "," + master + ")");
        
        // Check if limit for this master's reserved slave count has exceeded
        List<SlaveInstance> reservedSlaveInstances = slaveInstanceEJB.getSlaveInstancesReservedForMaster(master);
        int maxReservedLimit = slavePoolEJB.getMaxSlaveInstancesLimit(slavePool);
        if (maxReservedLimit != -1 && (reservedSlaveInstances.size() + slaveInstances > maxReservedLimit)) {
            slaveInstances = (long) maxReservedLimit - reservedSlaveInstances.size();
            if (slaveInstances < 1) {
                String result = this.gson.toJson(new ArrayList<SlaveView>());
                log.warn("Limit for max slave instances of pool {} for master {} has exceeded! No slaves returned", slavePool, master);
                loadBalancerIncidentHandlerEJB.maxSlaveInstancesForMasterExceeded(master, slavePool);
                return (Response.ok(result).header("X-LoadBalancer", "True").build());
           }
        }

        List<SlaveInstance> providedSlaveInstances = slaveInstanceEJB.provideSlaveInstances(slavePool, slaveLabel, slaveInstances.intValue());

        List<SlaveView> slaveViews = new ArrayList<SlaveView>();
        for (SlaveInstance slaveInstance : providedSlaveInstances) {
            if (!slaveInstanceEJB.reserveSlaveInstanceAtomic(slaveInstance.getId(), master)) {
                continue;
            }
            SlaveView sv = new SlaveView();

            sv.setCurrentMaster(master);

            sv.setExecutors(1);

            if (!StringUtils.isEmpty(slaveInstance.getUrl())) {
                sv.setHost(slaveInstance.getUrl());
            } else {
                sv.setHost(slaveInstance.getSlaveMachine().getUrl());
            }

            sv.setPort(slaveInstance.getSlaveMachine().getPort());

            StringBuilder stringBuilder = new StringBuilder(slaveInstance.getId().toString());
            stringBuilder.append("_");
            stringBuilder.append(slaveInstance.getSlaveMachine().getUrl());
            stringBuilder.append("_");
            stringBuilder.append(master);
            String modifiedName = stringBuilder.toString().replaceAll("http(s)?://", "")
                    .replaceAll(":", "_").replaceAll("/", "")
                    .replaceAll(".(europe|china|rnd|NOE).(n|N)okia.com", "");
            sv.setName(modifiedName);

            sv.setUsername("");
            sv.setPassword("");

            sv.setStartScript(slaveInstance.getSlaveMachine().getStartScript());
            sv.setEndScript(slaveInstance.getSlaveMachine().getEndScript());
            sv.setWorkspace(slaveInstance.getSlaveMachine().getWorkspace());

            slaveViews.add(sv);
        }
        String result = this.gson.toJson(slaveViews);
        //Fire slave stat event to collect load balancer statistical data.
        try{
            SlavePool statTargetSlavePool = slavePoolEJB.getSlavePoolByName(slavePool);
            if (statTargetSlavePool != null){
                loadBalancerStatEvents.fire(statTargetSlavePool.getId());
            }
        }catch(Exception slaveStatExc){
            log.error("Exception when fire slave stat event for pool {}. Detailed reason: {}", 
                    slavePool, slaveStatExc.getMessage() + slaveStatExc.getStackTrace());
        }
        log.info("Reserved {} slaves in {}ms", slaveViews.size(), System.currentTimeMillis() - startTime);
        return (Response.ok(result).header("X-LoadBalancer", "True").build());
    }

    /**
     * Release slave instance from master
     */
    private Response releaseSlaveInstance(String release_id) {
        log.info("SlaveResourceImpl.release( " + release_id + ")");

        String[] slaveInfo = release_id.split("_");
        Long id = Long.parseLong(slaveInfo[0]);
        try {
            slaveInstanceEJB.detach(id);
            return (Response.ok(this.gson.toJson(true)).header("X-LoadBalancer", "True").build());
        } catch (NotFoundException ex) {
            log.info("Trying to release non existing slave instance {}", release_id);
            return (Response.status(Response.Status.NOT_FOUND).header("X-LoadBalancer", "True").build());
        }
    }
}
