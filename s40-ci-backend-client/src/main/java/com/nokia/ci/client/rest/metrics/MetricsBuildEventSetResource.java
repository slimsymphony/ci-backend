package com.nokia.ci.client.rest.metrics;

import com.nokia.ci.client.model.MetricsBuildEventSetView;
import javax.annotation.security.DenyAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Interface for logging build event sets.
 *
 * @author jajuutin
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("metricsBuildEventSet")
@DenyAll
public interface MetricsBuildEventSetResource {

    /**
     * Process metrics event set view.
     *
     * @param mesv MetricsEventSetView to be processed.
     * @return HTTP response.
     */
    @POST
    Response processMetricsEventSet(MetricsBuildEventSetView mbesv);
}
