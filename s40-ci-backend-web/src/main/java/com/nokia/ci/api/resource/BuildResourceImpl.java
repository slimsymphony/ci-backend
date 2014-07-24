package com.nokia.ci.api.resource;

import com.nokia.ci.api.util.BuildEventViewUtil;
import com.nokia.ci.api.util.BuildFailureViewUtil;
import com.nokia.ci.api.util.MemConsumptionViewUtil;
import com.nokia.ci.api.util.TestCaseStatViewUtil;
import com.nokia.ci.api.util.TestCoverageViewUtil;
import com.nokia.ci.api.util.ViewModelUtils;
import com.nokia.ci.client.model.BuildEventView;
import com.nokia.ci.client.model.BuildFailureView;
import com.nokia.ci.client.model.BuildView;
import com.nokia.ci.client.model.MemConsumptionView;
import com.nokia.ci.client.model.MetricsDatasetView;
import com.nokia.ci.client.model.TestCaseStatView;
import com.nokia.ci.client.model.TestCoverageView;
import com.nokia.ci.client.rest.BuildResource;
import com.nokia.ci.ejb.BuildEJB;
import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildEvent;
import com.nokia.ci.ejb.model.BuildFailure;
import com.nokia.ci.ejb.model.Component;
import com.nokia.ci.ejb.model.MemConsumption;
import com.nokia.ci.ejb.model.TestCaseStat;
import com.nokia.ci.ejb.model.TestCoverage;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST resource for {@link Build} entities.
 *
 * @author vrouvine
 */
@Named
@RequestScoped
public class BuildResourceImpl implements BuildResource {

    private static Logger log = LoggerFactory.getLogger(BuildResourceImpl.class);
    /**
     * Build EJB.
     */
    @Inject
    BuildEJB buildEJB;
    @Inject
    BuildGroupEJB buildGroupEJB;

    @Override
    public Response getBuild(Long id) {
        log.debug("Requesting build by id {}", id);
        Build b = null;
        try {
            b = buildEJB.read(id);
        } catch (NotFoundException ex) {
            log.debug("Build not found", ex);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        BuildView view = ViewModelUtils.getModel(b, BuildView.class);
        return Response.ok(view).build();
    }

    @Override
    public Response createBuild(BuildView build) throws URISyntaxException {
        Build b = build.transformTo(Build.class);
        buildEJB.create(b);
        URI uri = UriBuilder.fromResource(
                BuildResource.class).path(b.getId().toString()).build();
        log.debug("New build created to uri: {}", uri.toString());
        return Response.created(uri).build();
    }

    @Override
    public Response getBuilds(String refSpec) {
        if (StringUtils.isEmpty(refSpec)) {
            log.warn("Requesting builds with empty refSpec!");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        log.debug("Requesting builds by refSpec {}", refSpec);
        List<Build> builds = buildEJB.getBuildsByRefSpec(refSpec);
        List<BuildView> views = ViewModelUtils.copyValuesFromList(builds, BuildView.class);
        return Response.ok(views).build();
    }

    @Override
    public Response createBuildEvents(Long id, List<BuildEventView> buildEventViews) {
        if (id == null) {
            log.warn("build id is null when creating build event.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (buildEventViews == null || buildEventViews.isEmpty()) {
            log.warn("build events not specified for creation.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (!BuildEventViewUtil.sanityCheck(buildEventViews)) {
            log.warn("build events do not pass sanity check.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // Convert view to EJB datamodel.
        List<BuildEvent> buildEvents = BuildEventViewUtil.transformBuildEvents(buildEventViews);

        try {
            // Store for build.
            buildEJB.addBuildEvents(id, buildEvents);
        } catch (NotFoundException ex) {
            log.warn("Metrics event logging attempted for unexisting build. cause: {}", ex.getMessage());
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Done.
        return Response.status(Response.Status.CREATED).build();
    }

    @Override
    public Response setExecutor(Long id, String executor) {
        log.debug("Set executor={} for build with id {}", executor, id);
        try {
            Build build = buildEJB.read(id);
            build.setExecutor(executor);
            buildEJB.update(build);
        } catch (NotFoundException ex) {
            log.debug("Build not found", ex);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.OK).build();
    }

    @Override
    public Response createMemConsumptions(Long id, List<MemConsumptionView> memConsumptionViews) {

        if (id == null) {
            log.warn("build id is null when creating build event.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (memConsumptionViews == null || memConsumptionViews.isEmpty()) {
            log.warn("memory consumptions are not specified for creation.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (!MemConsumptionViewUtil.sanityCheck(memConsumptionViews)) {
            log.warn("memory consumptions do not pass sanity check.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // Convert view to EJB datamodel.
        Map<Component, MemConsumption> componentConsumptions = MemConsumptionViewUtil.transformMemConsumptions(memConsumptionViews);

        try {
            Build build = buildEJB.read(id);
            Long buildGroupId = build.getBuildGroup().getId();

            buildGroupEJB.addComponents(buildGroupId, new ArrayList(componentConsumptions.keySet()));

            MemConsumptionViewUtil.fillPersistentObjectsToMemConsumptions(build, buildGroupEJB.getComponents(buildGroupId), componentConsumptions);

            buildEJB.addMemConsumptions(id, new ArrayList(componentConsumptions.values()));

        } catch (NotFoundException ex) {
            log.warn("Metrics memory consumption logging attempted for unexisting build. cause: {}", ex.getMessage());
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Done.
        return Response.status(Response.Status.CREATED).build();
    }

    @Override
    public Response createTestCaseStats(Long id, List<TestCaseStatView> testCaseStatViews) {

        if (id == null) {
            log.warn("build id is null when creating build event.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (testCaseStatViews == null || testCaseStatViews.isEmpty()) {
            log.warn("test case stats are not specified for creation.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (!TestCaseStatViewUtil.sanityCheck(testCaseStatViews)) {
            log.warn("test case stats do not pass sanity check.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // Convert view to EJB datamodel.
        Map<Component, TestCaseStat> componentTestCaseStats = TestCaseStatViewUtil.transformTestCaseStats(testCaseStatViews);

        try {
            Build build = buildEJB.read(id);
            Long buildGroupId = build.getBuildGroup().getId();

            buildGroupEJB.addComponents(buildGroupId, new ArrayList(componentTestCaseStats.keySet()));

            TestCaseStatViewUtil.fillPersistentObjectsToTestCaseStats(build, buildGroupEJB.getComponents(buildGroupId), componentTestCaseStats);

            buildEJB.addTestCaseStats(id, new ArrayList(componentTestCaseStats.values()));

        } catch (NotFoundException ex) {
            log.warn("Metrics memory consumption logging attempted for unexisting build. cause: {}", ex.getMessage());
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Done.
        return Response.status(Response.Status.CREATED).build();
    }

    @Override
    public Response createTestCoverages(Long id, List<TestCoverageView> testCoverageViews) {

        if (id == null) {
            log.warn("build id is null when creating build event.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (testCoverageViews == null || testCoverageViews.isEmpty()) {
            log.warn("test coverages are not specified for creation.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (!TestCoverageViewUtil.sanityCheck(testCoverageViews)) {
            log.warn("test coverages do not pass sanity check.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // Convert view to EJB datamodel.
        Map<Component, TestCoverage> componentTestCoverages = TestCoverageViewUtil.transformTestCoverages(testCoverageViews);

        try {
            Build build = buildEJB.read(id);
            Long buildGroupId = build.getBuildGroup().getId();

            buildGroupEJB.addComponents(buildGroupId, new ArrayList(componentTestCoverages.keySet()));

            TestCoverageViewUtil.fillPersistentObjectsToTestCoverages(build, buildGroupEJB.getComponents(buildGroupId), componentTestCoverages);

            buildEJB.addTestCoverages(id, new ArrayList(componentTestCoverages.values()));

        } catch (NotFoundException ex) {
            log.warn("Metrics memory consumption logging attempted for unexisting build. cause: {}", ex.getMessage());
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Done.
        return Response.status(Response.Status.CREATED).build();
    }
    
    @Override
    public Response createBuildFailures(Long id, List<BuildFailureView> buildFailureViews) {
        
        if (id == null) {
            log.warn("build id is null when creating build failures.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (buildFailureViews == null || buildFailureViews.isEmpty()) {
            log.warn("build failures not specified for creation.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (!BuildFailureViewUtil.sanityCheck(buildFailureViews)) {
            log.warn("build failures do not pass sanity check.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // Convert view to EJB datamodel.
        List<BuildFailure> buildFailures = BuildFailureViewUtil.transformBuildFailures(buildFailureViews);

        try {
            // Store for build.
            buildEJB.addBuildFailures(id, buildFailures);
        } catch (NotFoundException ex) {
            log.warn("Metrics build failure logging attempted for unexisting build. cause: {}", ex.getMessage());
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Done.
        return Response.status(Response.Status.CREATED).build();
        
    }

    @Override
    public Response createMetricsData(Long id, MetricsDatasetView metricsDatasetView) {

        Response buildResourceResponse;

        if (metricsDatasetView.getBuildEventViews() != null && metricsDatasetView.getBuildEventViews().size() > 0) {

            buildResourceResponse = createBuildEvents(id, metricsDatasetView.getBuildEventViews());

            if (buildResourceResponse.getStatus() != Response.Status.CREATED.getStatusCode()) {
                return buildResourceResponse;
            }
        }

        if (metricsDatasetView.getExecutor() != null) {

            buildResourceResponse = setExecutor(metricsDatasetView.getBuildId(), metricsDatasetView.getExecutor());

            if (buildResourceResponse.getStatus() != Response.Status.OK.getStatusCode()) {
                return buildResourceResponse;
            }
        }

        if (metricsDatasetView.getMemConsumptions() != null && metricsDatasetView.getMemConsumptions().size() > 0) {

            buildResourceResponse = createMemConsumptions(metricsDatasetView.getBuildId(), metricsDatasetView.getMemConsumptions());

            if (buildResourceResponse.getStatus() != Response.Status.CREATED.getStatusCode()) {
                return buildResourceResponse;
            }
        }

        if (metricsDatasetView.getTestCaseStats() != null && metricsDatasetView.getTestCaseStats().size() > 0) {

            buildResourceResponse = createTestCaseStats(metricsDatasetView.getBuildId(), metricsDatasetView.getTestCaseStats());

            if (buildResourceResponse.getStatus() != Response.Status.CREATED.getStatusCode()) {
                return buildResourceResponse;
            }
        }

        if (metricsDatasetView.getTestCoverages() != null && metricsDatasetView.getTestCoverages().size() > 0) {

            buildResourceResponse = createTestCoverages(metricsDatasetView.getBuildId(), metricsDatasetView.getTestCoverages());

            if (buildResourceResponse.getStatus() != Response.Status.CREATED.getStatusCode()) {
                return buildResourceResponse;
            }
        }
        
        if (metricsDatasetView.getBuildFailures() != null && metricsDatasetView.getBuildFailures().size() > 0) {
            
            buildResourceResponse = createBuildFailures(metricsDatasetView.getBuildId(), metricsDatasetView.getBuildFailures());
            
            if (buildResourceResponse.getStatus() != Response.Status.CREATED.getStatusCode()) {
                return buildResourceResponse;
            }
        }

        return Response.status(Response.Status.CREATED).build();
    }
}
