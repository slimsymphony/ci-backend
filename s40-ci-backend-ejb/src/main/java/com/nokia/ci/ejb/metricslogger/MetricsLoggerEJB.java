/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.metricslogger;

import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.event.BuildGroupFinishedEvent;
import com.nokia.ci.ejb.event.BuildGroupStartedEvent;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.metrics.MetricsClient;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.SysConfig;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.util.ConsistencyValidator;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 */
@Stateless
@LocalBean
public class MetricsLoggerEJB {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(MetricsLoggerEJB.class);
    @EJB
    public BuildGroupEJB buildGroupEJB;
    @EJB
    public ProjectEJB projectEJB;
    @EJB
    public SysConfigEJB sysConfigEJB;
    private int HTTP_TIMEOUT = 7000;
    
    @Asynchronous
    public void buildGroupStarted(@Observes(during = TransactionPhase.AFTER_SUCCESS) @BuildGroupStartedEvent Long id) 
            throws NotFoundException {
        
        logMetricsEvent(id);
    }

    @Asynchronous
    public void buildGroupFinished(@Observes(during = TransactionPhase.AFTER_SUCCESS) @BuildGroupFinishedEvent Long id) 
            throws NotFoundException {
        
        logMetricsEvent(id);
    }
    
    /**
     * Send event to metrics server.
     *
     * TODO: when adding support for multiple change sets per build this method
     * requires changes.
     *
     * @param id
     * @throws NotFoundException
     */
    private void logMetricsEvent(Long id) throws NotFoundException {
        // Retrieve metrics url.
        SysConfig metricsUrlConfig;
        try {
            metricsUrlConfig = sysConfigEJB.getSysConfig(SysConfigKey.METRICS_SERVER_URL);
        } catch (NotFoundException ex) {
            log.warn("Metrics server url not configured into system configuration. Discarding log request.");
            return;
        }

        String metricsUrl = metricsUrlConfig.getConfigValue();
        if (StringUtils.isEmpty(metricsUrl)) {
            log.warn("Metrics server url configured but is value set as empty string. Discarding log request.");
            return;
        }

        // Retrieve connection settings.
        int connectionTimeout = sysConfigEJB.getValue(SysConfigKey.HTTP_CLIENT_CONNECTION_TIMEOUT, HTTP_TIMEOUT);
        int socketTimeout = sysConfigEJB.getValue(SysConfigKey.HTTP_CLIENT_SOCKET_TIMEOUT, HTTP_TIMEOUT);

        // Fetch data for metrics log event.
        BuildGroup buildGroup = buildGroupEJB.read(id);
        ConsistencyValidator.validate(buildGroup);

        Date date;
        if (buildGroup.getEndTime() != null) {
            date = buildGroup.getEndTime();
        } else if (buildGroup.getStartTime() != null) {
            date = buildGroup.getStartTime();
        } else {
            date = new Date();
        }

        if (buildGroup.getJob() == null) {
            return;
        }

        Branch branch = buildGroup.getJob().getBranch();

        if (branch.getProject() == null) {
            return;
        }

        Project project = branch.getProject();
        List<String> changeSetIds = new ArrayList<String>();
        // TODO: add change set list handling here
        changeSetIds.add(buildGroup.getGerritPatchSetRevision());

        // Log metrics event.
        MetricsClient metricsClient = new MetricsClient(metricsUrl, connectionTimeout,
                socketTimeout);
        metricsClient.log(date, project.getName(), id,
                branch.getType().toString(), buildGroup.getPhase().toString(),
                buildGroup.getStatus().toString(), changeSetIds);
    }    
}
