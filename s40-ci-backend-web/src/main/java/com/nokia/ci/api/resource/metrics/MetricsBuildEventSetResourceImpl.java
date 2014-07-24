/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.api.resource.metrics;

import com.nokia.ci.api.util.BuildEventViewUtil;
import com.nokia.ci.client.model.MetricsBuildEventSetView;
import com.nokia.ci.client.rest.metrics.MetricsBuildEventSetResource;
import com.nokia.ci.ejb.BuildEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.BuildEvent;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 */
@Named
@RequestScoped
public class MetricsBuildEventSetResourceImpl implements MetricsBuildEventSetResource {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MetricsBuildEventSetResourceImpl.class);
    @Inject
    BuildEJB buildEJB;

    @Override
    public Response processMetricsEventSet(MetricsBuildEventSetView mbesv) {
        if (!sanityCheck(mbesv)) {
            log.warn("Invalid json received to metrics event rest point.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // Convert view to EJB datamodel.
        List<BuildEvent> buildEvents = BuildEventViewUtil.transformBuildEvents(mbesv.getEvents());

        try {
            // Store for build.
            buildEJB.addBuildEvents(mbesv.getBuildId(), buildEvents, mbesv.getExecutor());
        } catch (NotFoundException ex) {
            log.warn("Metrics event logging attempted for unexisting build {}. cause: ",
                    mbesv.getBuildId(), ex.getMessage());
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Done.
        return Response.status(Response.Status.CREATED).build();
    }

    /**
     * Check message sanity.
     *
     * @param mbesv
     * @return
     */
    private boolean sanityCheck(MetricsBuildEventSetView mbesv) {
        if (mbesv == null || mbesv.getBuildId() == null || mbesv.getEvents() == null || mbesv.getEvents().isEmpty()) {
            return false;
        }

        if (!BuildEventViewUtil.sanityCheck(mbesv.getEvents())) {
            return false;
        }

        return true;
    }
}
