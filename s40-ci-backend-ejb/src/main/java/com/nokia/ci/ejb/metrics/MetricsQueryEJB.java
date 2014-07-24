/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.metrics;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildFailure;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildGroup_;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.model.BuildVerificationConf;
import com.nokia.ci.ejb.model.BuildVerificationConf_;
import com.nokia.ci.ejb.model.Build_;
import com.nokia.ci.ejb.model.ChangeTracker;
import com.nokia.ci.ejb.model.ChangeTracker_;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.MemConsumption;
import com.nokia.ci.ejb.model.MemConsumption_;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.Project_;
import com.nokia.ci.ejb.model.SlaveLabel;
import com.nokia.ci.ejb.model.SlaveMachine;
import com.nokia.ci.ejb.model.SlavePool;
import com.nokia.ci.ejb.model.SlaveStatPerLabel;
import com.nokia.ci.ejb.model.SlaveStatPerLabel_;
import com.nokia.ci.ejb.model.SlaveStatPerMachine;
import com.nokia.ci.ejb.model.SlaveStatPerMachine_;
import com.nokia.ci.ejb.model.SlaveStatPerPool;
import com.nokia.ci.ejb.model.SlaveStatPerPool_;
import com.nokia.ci.ejb.model.TestCaseStat;
import com.nokia.ci.ejb.model.TestCoverage;
import com.nokia.ci.ejb.model.TestResultType;
import com.nokia.ci.ejb.util.Order;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 *
 * EJB for metrics specific database queries.
 *
 */
@Stateless
@LocalBean
public class MetricsQueryEJB {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MetricsQueryEJB.class);
    @PersistenceContext(unitName = "NokiaCI-PU")
    EntityManager em;
    CriteriaBuilder cb;
    
    public static final String KW_NOT_ANALYZED = "Not analyzed";

    @PostConstruct
    public void init() {
        cb = em.getCriteriaBuilder();
    }

    public List<MetricsVerification> getCompletedVerifications(Long id, Date searchStart, Date searchEnd, Order order) throws NotFoundException {

        // Log.
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Finding buildgroups for job:").append(id);
            sb.append(" with searchStart:").append(searchStart);
            sb.append(" and searchEnd: ").append(searchEnd);
            log.debug(sb.toString());
        }

        // Get job.
        Job job = em.find(Job.class, id);
        if (job == null) {
            throw new NotFoundException(id, Job.class);
        }

        // Create query.
        CriteriaQuery<MetricsVerification> query = cb.createQuery(MetricsVerification.class);

        // Create root object.
        Root<BuildGroup> buildGroup = query.from(BuildGroup.class);

        // Load only necessary attributes to speed up search.
        query.select(cb.construct(MetricsVerification.class,
                buildGroup.get(BuildGroup_.id), buildGroup.get(BuildGroup_.status),
                buildGroup.get(BuildGroup_.startTime), buildGroup.get(BuildGroup_.endTime)));

        // Create predicates.
        Predicate predicateJobId = cb.equal(buildGroup.get(BuildGroup_.job), job);
        Predicate predicateStartTimeNotNull = cb.isNotNull(buildGroup.get(BuildGroup_.startTime));
        Predicate predicateEndTimeNotNull = cb.isNotNull(buildGroup.get(BuildGroup_.endTime));
        Predicate predicateStartLimit = cb.greaterThanOrEqualTo(buildGroup.get(BuildGroup_.startTime), searchStart);
        Predicate predicateEndTimeLimit = cb.lessThan(buildGroup.get(BuildGroup_.endTime), searchEnd);

        // Insert predicates.
        query.where(cb.and(predicateJobId, predicateStartTimeNotNull,
                predicateEndTimeNotNull, predicateStartLimit, predicateEndTimeLimit));

        // Set order by.
        if (order == null || order == Order.ASC) {
            query.orderBy(cb.asc(buildGroup.get(BuildGroup_.startTime)));
        } else {
            query.orderBy(cb.desc(buildGroup.get(BuildGroup_.startTime)));
        }

        // Execute query.
        List<MetricsVerification> metricsVerifications = em.createQuery(query).getResultList();

        // Log completion.
        log.debug("Finding builds completed");

        // Return results.
        return metricsVerifications;
    }

    public List<MetricsVerification> getCompletedSubVerifications(Long id, Date searchStart, Date searchEnd, Order order) throws NotFoundException {
        CriteriaQuery<MetricsVerification> query = cb.createQuery(MetricsVerification.class);
        Root<BuildGroup> buildGroup = query.from(BuildGroup.class);
        ListJoin<BuildGroup, Build> builds = buildGroup.join(BuildGroup_.builds);

        // Load only necessary attributes to speed up search.
        query.select(cb.construct(MetricsVerification.class,
                builds.get(Build_.id), builds.get(Build_.status),
                builds.get(Build_.startTime), builds.get(Build_.endTime)));

        // Create predicates.
        Predicate predicateJobId = cb.equal(buildGroup.get(BuildGroup_.job), id);
        Predicate predicateStartTimeNotNull = cb.isNotNull(builds.get(Build_.startTime));
        Predicate predicateEndTimeNotNull = cb.isNotNull(builds.get(Build_.endTime));
        Predicate predicateStartLimit = cb.greaterThanOrEqualTo(builds.get(Build_.startTime), searchStart);
        Predicate predicateEndTimeLimit = cb.lessThan(builds.get(Build_.endTime), searchEnd);

        // Insert predicates.
        query.where(cb.and(predicateJobId, predicateStartTimeNotNull,
                predicateEndTimeNotNull, predicateStartLimit, predicateEndTimeLimit));

        // Set order by.
        if (order == null || order == Order.ASC) {
            query.orderBy(cb.asc(builds.get(Build_.startTime)));
        } else {
            query.orderBy(cb.desc(builds.get(Build_.startTime)));
        }

        List<MetricsVerification> results = em.createQuery(query).getResultList();
        return results;
    }

    /**
     * Get change trackers from specified time interval with specified hangtime type.
     * 
     * Hangtime type is relevant, since it affects database query quite a bit.
     * e.g. some fields can be null with other hangtime type, but yet another 
     * type requires them.
     * 
     * @param projectId
     * @param searchStart
     * @param searchEnd
     * @param order
     * @param htType
     * @return 
     */
    public List<ChangeTracker> getChangeTrackers(Long projectId, Date searchStart, Date searchEnd, MetricsHangtimeType htType) {
        // Create query.
        CriteriaQuery<ChangeTracker> query = cb.createQuery(ChangeTracker.class);

        // Create root object.
        Root<ChangeTracker> changeTracker = query.from(ChangeTracker.class);

        // Join to projects list. restrict by project id.
        ListJoin<ChangeTracker, Project> projects = changeTracker.join(ChangeTracker_.projects);
        Predicate predicates = cb.equal(projects.get(Project_.id), projectId);        

        // Commit id predicate
        predicates = cb.and(predicates, cb.isNotNull(changeTracker.get(ChangeTracker_.commitId)));        
        
        // Hangtime type specific predicates.
        if(htType == MetricsHangtimeType.DEVELOPMENT) {
            predicates = cb.and(predicates, cb.isNotNull(changeTracker.get(ChangeTracker_.scvStart)));
            predicates = cb.and(predicates, cb.isNotNull(changeTracker.get(ChangeTracker_.dbvEnd)));
            predicates = cb.and(predicates, cb.greaterThanOrEqualTo(changeTracker.get(ChangeTracker_.scvStart), searchStart));
            predicates = cb.and(predicates, cb.lessThan(changeTracker.get(ChangeTracker_.scvStart), searchEnd));
            query.orderBy(cb.asc(changeTracker.get(ChangeTracker_.scvStart)));            
        } else if(htType == MetricsHangtimeType.INTEGRATION) {
            predicates = cb.and(predicates, cb.isNotNull(changeTracker.get(ChangeTracker_.dbvEnd)));
            predicates = cb.and(predicates, cb.isNotNull(changeTracker.get(ChangeTracker_.released)));            
            predicates = cb.and(predicates, cb.greaterThanOrEqualTo(changeTracker.get(ChangeTracker_.dbvEnd), searchStart));
            predicates = cb.and(predicates, cb.lessThan(changeTracker.get(ChangeTracker_.dbvEnd), searchEnd));
            query.orderBy(cb.asc(changeTracker.get(ChangeTracker_.dbvEnd)));            
        } else if(htType == MetricsHangtimeType.DELIVERY_CHAIN) {
            predicates = cb.and(predicates, cb.isNotNull(changeTracker.get(ChangeTracker_.scvStart)));
            predicates = cb.and(predicates, cb.isNotNull(changeTracker.get(ChangeTracker_.released)));
            predicates = cb.and(predicates, cb.greaterThanOrEqualTo(changeTracker.get(ChangeTracker_.scvStart), searchStart));
            predicates = cb.and(predicates, cb.lessThan(changeTracker.get(ChangeTracker_.scvStart), searchEnd));
            query.orderBy(cb.asc(changeTracker.get(ChangeTracker_.scvStart)));            
        }
        
        // Insert predicates.
        query.where(predicates);

        // Execute query.
        List<ChangeTracker> cts = em.createQuery(query).getResultList();

        // Done.
        return cts;
    }

    
    public List<MetricsBuild> getCompletedBuilds(Long id, Date searchStart, Date searchEnd, Order order, boolean latestPassedOnly) throws NotFoundException {

        // Log.
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Finding same kind of builds like build:").append(id);
            sb.append(" with searchStart:").append(searchStart);
            sb.append(" and searchEnd: ").append(searchEnd);
            log.debug(sb.toString());
        }
        
        if (latestPassedOnly){
            //Enforce order for latestOnly
            order = Order.DESC;
        }

        // Get build.
        Build refBuild = em.find(Build.class, id);
        if (refBuild == null || refBuild.getBuildGroup() == null || refBuild.getBuildGroup().getJob() == null) {
            throw new NotFoundException(id, Build.class);
        }
        
        if (refBuild.getBuildVerificationConf() == null || refBuild.getBuildVerificationConf().getVerificationUuid() == null ||
                refBuild.getBuildVerificationConf().getProductUuid() == null){
            //Return empty dataset to make backward compatibility.
            return new ArrayList<MetricsBuild>();
        }

        CriteriaQuery<MetricsBuild> query = cb.createQuery(MetricsBuild.class);
        Root<Build> build = query.from(Build.class);
        Join<Build, BuildVerificationConf> buildConf = build.join(Build_.buildVerificationConf);
        Join<Build, BuildGroup> buildGroup = build.join(Build_.buildGroup);

        // Load only necessary attributes to speed up search.
        query.select(cb.construct(MetricsBuild.class,
                build.get(Build_.id), build.get(Build_.status),
                build.get(Build_.startTime), build.get(Build_.endTime),
                buildConf.get(BuildVerificationConf_.verificationUuid),
                buildConf.get(BuildVerificationConf_.productUuid)));

        // Create predicates.
        Predicate predicateVerificationUuid = cb.equal(buildConf.get(BuildVerificationConf_.verificationUuid), refBuild.getBuildVerificationConf().getVerificationUuid());
        Predicate predicateProductUuid = cb.equal(buildConf.get(BuildVerificationConf_.productUuid), refBuild.getBuildVerificationConf().getProductUuid());
        Predicate predicateStartTimeNotNull = cb.isNotNull(build.get(Build_.startTime));
        Predicate predicateEndTimeNotNull = cb.isNotNull(build.get(Build_.endTime));
        Predicate predicateStartLimit = cb.greaterThanOrEqualTo(build.get(Build_.startTime), searchStart);
        Predicate predicateEndTimeLimit = cb.lessThan(build.get(Build_.endTime), searchEnd);
        Predicate predicateBuildGroup = cb.equal(buildGroup.get(BuildGroup_.job), refBuild.getBuildGroup().getJob().getId());
        
        // Insert predicates.
        if (latestPassedOnly){
            Predicate predicateStatus = cb.or(cb.equal(build.get(Build_.status), BuildStatus.SUCCESS),
                                                cb.equal(build.get(Build_.status), BuildStatus.UNSTABLE));
            query.where(cb.and(predicateVerificationUuid, predicateProductUuid, predicateStartTimeNotNull, predicateEndTimeNotNull,
                    predicateStartLimit, predicateEndTimeLimit, predicateBuildGroup, predicateStatus));
        }else{
            query.where(cb.and(predicateVerificationUuid, predicateProductUuid, predicateStartTimeNotNull, predicateEndTimeNotNull,
                    predicateStartLimit, predicateEndTimeLimit, predicateBuildGroup));
        }

        // Set order by.
        if (order == null || order == Order.ASC) {
            query.orderBy(cb.asc(build.get(Build_.startTime)));
        } else {
            query.orderBy(cb.desc(build.get(Build_.startTime)));
        }

        List<MetricsBuild> results = null;
        if (latestPassedOnly){
            results = new ArrayList<MetricsBuild>();
            MetricsBuild onlyResult = em.createQuery(query).setMaxResults(1).getSingleResult();
            if (onlyResult != null){
                results.add(onlyResult);
                log.info("Found latest build with id: {}.", onlyResult.getId());
            }
        }else{
            results = em.createQuery(query).getResultList();
        }
        
        return results;
    }
    
    public List<MetricsBuild> getCompletedBuildsWhichTriggerTest(Long id, Date searchStart, Date searchEnd, Order order) throws NotFoundException {
        
        List<MetricsBuild> results = getCompletedBuilds(id, searchStart, searchEnd, order, false);
        
        for (MetricsBuild metricsBuild : results){
            
            Build build = em.find(Build.class, metricsBuild.getId());
            
            if (!build.getTestCaseStats().isEmpty()){
                
                metricsBuild.setTriggerTest(true);
            }
        }
        
        return results;
    }
    
    public List<MetricsBuildMemConsumption> getMemConsumptionOfCompletedBuilds
            (Long id, Date searchStart, Date searchEnd, Order order, String componentName) throws NotFoundException {
        
        List<MetricsBuildMemConsumption> results = new ArrayList<MetricsBuildMemConsumption>();
        
        List<MetricsBuild> metricsBuilds = getCompletedBuilds(id, searchStart, searchEnd, order, false);
        
        for (MetricsBuild metricsBuild : metricsBuilds){
            
            MetricsBuildMemConsumption metricsBuildMemConsumption = new MetricsBuildMemConsumption(metricsBuild);
            
            Build build = em.find(Build.class, metricsBuild.getId());
            
            boolean foundTestData = false;
            
            for (MemConsumption memConsumption : build.getMemConsumptions()){
                
                if (memConsumption.getComponent().getName().equals(componentName)){
                    metricsBuildMemConsumption.setComponentName(memConsumption.getComponent().getName());
                    metricsBuildMemConsumption.setRam(memConsumption.getRam());
                    metricsBuildMemConsumption.setRom(memConsumption.getRom());
                    foundTestData = true;
                    break;
                }
            }
            
            if (foundTestData){
                results.add(metricsBuildMemConsumption);
            }
        }
 
        return results;
    }
    
    public List<MetricsBuildMemConsumption> getAllMemConsumptionsOfLatestBuild
            (Long id, Date searchStart, Date searchEnd) throws NotFoundException {

        List<MetricsBuildMemConsumption> results = new ArrayList<MetricsBuildMemConsumption>();
        
        try{
            List<MetricsBuild> metricsBuilds = getCompletedBuilds(id, searchStart, searchEnd, Order.DESC, true);

            if (metricsBuilds != null && metricsBuilds.size() > 0){

                MetricsBuild latestMetricsBuild = metricsBuilds.get(0);
                Build build = em.find(Build.class, latestMetricsBuild.getId());

                for (MemConsumption memConsumption : build.getMemConsumptions()){

                    MetricsBuildMemConsumption metricsBuildMemConsumption = new MetricsBuildMemConsumption(latestMetricsBuild);

                    metricsBuildMemConsumption.setComponentName(memConsumption.getComponent().getName());
                    metricsBuildMemConsumption.setRam(memConsumption.getRam());
                    metricsBuildMemConsumption.setRom(memConsumption.getRom());

                    results.add(metricsBuildMemConsumption);
                }
            }else{
                log.warn("Can not find latest build similiar with build id {}.", id);
            }
        }catch(Exception e){
            log.warn("Exceptoin when getting latest build similiar with build id {}.", id);
        }
        return results; 
    }
    
    public List<MetricsBuildTestCaseStat> getTestCaseStatOfCompletedBuilds
            (Long id, Date searchStart, Date searchEnd, Order order, String componentName) throws NotFoundException {
        
        List<MetricsBuildTestCaseStat> results = new ArrayList<MetricsBuildTestCaseStat>();
        
        List<MetricsBuild> metricsBuilds = getCompletedBuilds(id, searchStart, searchEnd, order, false);
        
        for (MetricsBuild metricsBuild : metricsBuilds){
            
            MetricsBuildTestCaseStat metricsBuildTestCaseStat = new MetricsBuildTestCaseStat(metricsBuild);
            
            Build build = em.find(Build.class, metricsBuild.getId());
            
            boolean foundTestData = false;
            
            for (TestCaseStat testCaseStat : build.getTestCaseStats()){
                
                if (testCaseStat.getComponent().getName().equals(componentName)){
                    metricsBuildTestCaseStat.setComponentName(testCaseStat.getComponent().getName());
                    metricsBuildTestCaseStat.setPassedCount(testCaseStat.getPassedCount());
                    metricsBuildTestCaseStat.setFailedCount(testCaseStat.getFailedCount());
                    metricsBuildTestCaseStat.setNaCount(testCaseStat.getNaCount());
                    metricsBuildTestCaseStat.setTotalCount(testCaseStat.getTotalCount());
                    foundTestData = true;
                    break;
                }
            }
            
            if (foundTestData){
                results.add(metricsBuildTestCaseStat);
            }
        }
 
        return results;
    }
    
    public List<MetricsBuildTestCaseStat> getAllTestCaseStatsOfLatestBuild
            (Long id, Date searchStart, Date searchEnd) throws NotFoundException {

        List<MetricsBuildTestCaseStat> results = new ArrayList<MetricsBuildTestCaseStat>();
        
        try{
        
            List<MetricsBuild> metricsBuilds = getCompletedBuilds(id, searchStart, searchEnd, Order.DESC, true);

            if (metricsBuilds != null && metricsBuilds.size() > 0){

                MetricsBuild latestMetricsBuild = metricsBuilds.get(0);
                Build build = em.find(Build.class, latestMetricsBuild.getId());

                for (TestCaseStat testCaseStat : build.getTestCaseStats()){

                    MetricsBuildTestCaseStat metricsBuildTestCaseStat = new MetricsBuildTestCaseStat(latestMetricsBuild);

                    metricsBuildTestCaseStat.setComponentName(testCaseStat.getComponent().getName());
                    metricsBuildTestCaseStat.setPassedCount(testCaseStat.getPassedCount());
                    metricsBuildTestCaseStat.setFailedCount(testCaseStat.getFailedCount());
                    metricsBuildTestCaseStat.setNaCount(testCaseStat.getNaCount());
                    metricsBuildTestCaseStat.setTotalCount(testCaseStat.getTotalCount());

                    results.add(metricsBuildTestCaseStat);
                }
            }else{
                log.warn("Can not find latest build similiar with build id {}.", id);
            }
        }catch(Exception e){
            log.warn("Exceptoin when getting latest build similiar with build id {}.", id);
        }
        return results; 
    }
    
    public List<MetricsBuildTestCoverage> getTestCoverageOfCompletedBuilds
            (Long id, Date searchStart, Date searchEnd, Order order, String componentName) throws NotFoundException {
        
        List<MetricsBuildTestCoverage> results = new ArrayList<MetricsBuildTestCoverage>();
        
        List<MetricsBuild> metricsBuilds = getCompletedBuilds(id, searchStart, searchEnd, order, false);
        
        for (MetricsBuild metricsBuild : metricsBuilds){
            
            MetricsBuildTestCoverage metricsBuildTestCoverage = new MetricsBuildTestCoverage(metricsBuild);
            
            Build build = em.find(Build.class, metricsBuild.getId());
            
            boolean foundTestData = false;
            
            for (TestCoverage testCoverage : build.getTestCoverages()){
                
                if (testCoverage.getComponent().getName().equals(componentName)){
                    metricsBuildTestCoverage.setComponentName(testCoverage.getComponent().getName());
                    metricsBuildTestCoverage.setCondCov(testCoverage.getCondCov());
                    metricsBuildTestCoverage.setStmtCov(testCoverage.getStmtCov());
                    foundTestData = true;
                    break;
                }
            }
            
            if (foundTestData){
                results.add(metricsBuildTestCoverage);
            }
        }
 
        return results;
    }
    
    public List<MetricsBuildTestCoverage> getAllTestCoveragesOfLatestBuild
            (Long id, Date searchStart, Date searchEnd) throws NotFoundException {

        List<MetricsBuildTestCoverage> results = new ArrayList<MetricsBuildTestCoverage>();
        
        try{

            List<MetricsBuild> metricsBuilds = getCompletedBuilds(id, searchStart, searchEnd, Order.DESC, true);

            if (metricsBuilds != null && metricsBuilds.size() > 0){

                MetricsBuild latestMetricsBuild = metricsBuilds.get(0);
                Build build = em.find(Build.class, latestMetricsBuild.getId());

                for (TestCoverage testCoverage : build.getTestCoverages()){

                    MetricsBuildTestCoverage metricsBuildTestCoverage = new MetricsBuildTestCoverage(latestMetricsBuild);

                    metricsBuildTestCoverage.setComponentName(testCoverage.getComponent().getName());
                    metricsBuildTestCoverage.setCondCov(testCoverage.getCondCov());
                    metricsBuildTestCoverage.setStmtCov(testCoverage.getStmtCov());

                    results.add(metricsBuildTestCoverage);
                }
            }else{
                log.warn("Can not find latest build similiar with build id {}.", id);
            }
        }catch(Exception e){
            log.warn("Exceptoin when getting latest build similiar with build id {}.", id);
        }
        return results; 
    }
    
    public List<MetricsSlaveStat> getSlaveStatsPerPool(Long id, Date searchStart, Date searchEnd, Order order) throws NotFoundException {

        // Log.
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Finding slave stats for pool:").append(id);
            sb.append(" with searchStart:").append(searchStart);
            sb.append(" and searchEnd: ").append(searchEnd);
            log.debug(sb.toString());
        }

        // Get job.
        SlavePool slavePool = em.find(SlavePool.class, id);
        if (slavePool == null) {
            throw new NotFoundException(id, SlavePool.class);
        }

        // Create query.
        CriteriaQuery<MetricsSlaveStat> query = cb.createQuery(MetricsSlaveStat.class);

        // Create root object.
        Root<SlaveStatPerPool> slaveStatPerPool = query.from(SlaveStatPerPool.class);

        // Load only necessary attributes to speed up search.
        query.select(cb.construct(MetricsSlaveStat.class,
                slaveStatPerPool.get(SlaveStatPerPool_.id), slaveStatPerPool.get(SlaveStatPerPool_.provisionTime),
                slaveStatPerPool.get(SlaveStatPerPool_.reservedInstanceCount), slaveStatPerPool.get(SlaveStatPerPool_.totalInstanceCount)));

        // Create predicates.
        Predicate predicateSlavePoolId = cb.equal(slaveStatPerPool.get(SlaveStatPerPool_.slavePool), slavePool);
        Predicate predicateProvisionTimeNotNull = cb.isNotNull(slaveStatPerPool.get(SlaveStatPerPool_.provisionTime));
        Predicate predicateProvisionTimeBottomLimit = cb.greaterThanOrEqualTo(slaveStatPerPool.get(SlaveStatPerPool_.provisionTime), searchStart);
        Predicate predicateProvisionTimeUpperLimit = cb.lessThan(slaveStatPerPool.get(SlaveStatPerPool_.provisionTime), searchEnd);

        // Insert predicates.
        query.where(cb.and(predicateSlavePoolId, predicateProvisionTimeNotNull,
                predicateProvisionTimeBottomLimit, predicateProvisionTimeUpperLimit));

        // Set order by.
        if (order == null || order == Order.ASC) {
            query.orderBy(cb.asc(slaveStatPerPool.get(SlaveStatPerPool_.provisionTime)));
        } else {
            query.orderBy(cb.desc(slaveStatPerPool.get(SlaveStatPerPool_.provisionTime)));
        }

        // Execute query.
        List<MetricsSlaveStat> metricsSlaveStats = em.createQuery(query).getResultList();

        // Log completion.
        log.debug("Finding slave stats per pool completed");

        // Return results.
        return metricsSlaveStats;
    }
    
    public List<MetricsSlaveStat> getSlaveStatsPerLabel(Long slavePoolId, Long slaveLabelId, Date searchStart, Date searchEnd, Order order) throws NotFoundException {

        // Log.
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Finding slave stats for pool:").append(slavePoolId == null ? "NULL" : slavePoolId).append(" and label:").append(slaveLabelId);
            sb.append(" with searchStart:").append(searchStart);
            sb.append(" and searchEnd: ").append(searchEnd);
            log.debug(sb.toString());
        }

        // Get job.
        SlavePool slavePool = em.find(SlavePool.class, slavePoolId);
        if (slavePool == null) {
            throw new NotFoundException(slavePoolId, SlavePool.class);
        }
        
        SlaveLabel slaveLabel = null;
        
        if (slaveLabelId != null){
            slaveLabel = em.find(SlaveLabel.class, slaveLabelId);
            if (slaveLabel == null) {
                throw new NotFoundException(slaveLabelId, SlaveLabel.class);
            }
        }

        // Create query.
        CriteriaQuery<MetricsSlaveStat> query = cb.createQuery(MetricsSlaveStat.class);

        // Create root object.
        Root<SlaveStatPerLabel> slaveStatPerLabel = query.from(SlaveStatPerLabel.class);

        // Load only necessary attributes to speed up search.
        query.select(cb.construct(MetricsSlaveStat.class,
                slaveStatPerLabel.get(SlaveStatPerLabel_.id), slaveStatPerLabel.get(SlaveStatPerLabel_.provisionTime),
                slaveStatPerLabel.get(SlaveStatPerLabel_.reservedInstanceCount), slaveStatPerLabel.get(SlaveStatPerLabel_.totalInstanceCount)));

        // Create predicates.
        Predicate predicateSlavePoolId = cb.equal(slaveStatPerLabel.get(SlaveStatPerLabel_.slavePool), slavePool);
        Predicate predicateSlaveLabelId;
        if (slaveLabel != null){
            predicateSlaveLabelId = cb.equal(slaveStatPerLabel.get(SlaveStatPerLabel_.slaveLabel), slaveLabel);
        }else{
            predicateSlaveLabelId = cb.isNull(slaveStatPerLabel.get(SlaveStatPerLabel_.slaveLabel));
        }
        Predicate predicateProvisionTimeNotNull = cb.isNotNull(slaveStatPerLabel.get(SlaveStatPerLabel_.provisionTime));
        Predicate predicateProvisionTimeBottomLimit = cb.greaterThanOrEqualTo(slaveStatPerLabel.get(SlaveStatPerLabel_.provisionTime), searchStart);
        Predicate predicateProvisionTimeUpperLimit = cb.lessThan(slaveStatPerLabel.get(SlaveStatPerLabel_.provisionTime), searchEnd);

        // Insert predicates.
        query.where(cb.and(predicateSlavePoolId, predicateSlaveLabelId, predicateProvisionTimeNotNull,
                predicateProvisionTimeBottomLimit, predicateProvisionTimeUpperLimit));

        // Set order by.
        if (order == null || order == Order.ASC) {
            query.orderBy(cb.asc(slaveStatPerLabel.get(SlaveStatPerLabel_.provisionTime)));
        } else {
            query.orderBy(cb.desc(slaveStatPerLabel.get(SlaveStatPerLabel_.provisionTime)));
        }

        // Execute query.
        List<MetricsSlaveStat> metricsSlaveStats = em.createQuery(query).getResultList();

        // Log completion.
        log.debug("Finding slave stats per label completed");

        // Return results.
        return metricsSlaveStats;
    }
    
    public List<MetricsSlaveStat> getSlaveStatsPerMachine(Long slavePoolId, Long slaveMachineId, Date searchStart, Date searchEnd, Order order) throws NotFoundException {

        // Log.
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Finding slave stats for pool:").append(slavePoolId).append(" and machine:").append(slaveMachineId);
            sb.append(" with searchStart:").append(searchStart);
            sb.append(" and searchEnd: ").append(searchEnd);
            log.debug(sb.toString());
        }

        // Get job.
        SlavePool slavePool = em.find(SlavePool.class, slavePoolId);
        if (slavePool == null) {
            throw new NotFoundException(slavePoolId, SlavePool.class);
        }
        
        SlaveMachine slaveMachine = em.find(SlaveMachine.class, slaveMachineId);
        if (slaveMachine == null) {
            throw new NotFoundException(slaveMachineId, SlaveMachine.class);
        }

        // Create query.
        CriteriaQuery<MetricsSlaveStat> query = cb.createQuery(MetricsSlaveStat.class);

        // Create root object.
        Root<SlaveStatPerMachine> slaveStatPerMachine = query.from(SlaveStatPerMachine.class);

        // Load only necessary attributes to speed up search.
        query.select(cb.construct(MetricsSlaveStat.class,
                slaveStatPerMachine.get(SlaveStatPerMachine_.id), slaveStatPerMachine.get(SlaveStatPerMachine_.provisionTime),
                slaveStatPerMachine.get(SlaveStatPerMachine_.reservedInstanceCount), slaveStatPerMachine.get(SlaveStatPerMachine_.totalInstanceCount)));

        // Create predicates.
        Predicate predicateSlavePoolId = cb.equal(slaveStatPerMachine.get(SlaveStatPerMachine_.slavePool), slavePool);
        Predicate predicateSlaveMachineId = cb.equal(slaveStatPerMachine.get(SlaveStatPerMachine_.slaveMachine), slaveMachine);
        Predicate predicateProvisionTimeNotNull = cb.isNotNull(slaveStatPerMachine.get(SlaveStatPerMachine_.provisionTime));
        Predicate predicateProvisionTimeBottomLimit = cb.greaterThanOrEqualTo(slaveStatPerMachine.get(SlaveStatPerMachine_.provisionTime), searchStart);
        Predicate predicateProvisionTimeUpperLimit = cb.lessThan(slaveStatPerMachine.get(SlaveStatPerMachine_.provisionTime), searchEnd);

        // Insert predicates.
        query.where(cb.and(predicateSlavePoolId, predicateSlaveMachineId, predicateProvisionTimeNotNull,
                predicateProvisionTimeBottomLimit, predicateProvisionTimeUpperLimit));

        // Set order by.
        if (order == null || order == Order.ASC) {
            query.orderBy(cb.asc(slaveStatPerMachine.get(SlaveStatPerMachine_.provisionTime)));
        } else {
            query.orderBy(cb.desc(slaveStatPerMachine.get(SlaveStatPerMachine_.provisionTime)));
        }

        // Execute query.
        List<MetricsSlaveStat> metricsSlaveStats = em.createQuery(query).getResultList();

        // Log completion.
        log.debug("Finding slave stats per machine completed");

        // Return results.
        return metricsSlaveStats;
    }
    
    public List<MetricsBuildFailureCategory> getFailureCategoryOfCompletedBuilds
            (Long id, Date searchStart, Date searchEnd, Order order) throws NotFoundException {
        
        List<MetricsBuildFailureCategory> results = new ArrayList<MetricsBuildFailureCategory>();
        
        List<MetricsBuild> metricsBuilds = getCompletedBuilds(id, searchStart, searchEnd, order, false);
        
        for (MetricsBuild metricsBuild : metricsBuilds){
            
            MetricsBuildFailureCategory metricsBuildFailureCategory = new MetricsBuildFailureCategory(metricsBuild);
            
            Build build = em.find(Build.class, metricsBuild.getId());
            
            List<BuildFailure> buildFailures = build.getBuildFailures();
            
            for (BuildFailure buildFailure : buildFailures){
                
                if (buildFailure.getFailureReason() != null && StringUtils.isNotEmpty(buildFailure.getFailureReason().getName())){
                    metricsBuildFailureCategory.addFailure(buildFailure.getFailureReason().getName());
                }else{
                    metricsBuildFailureCategory.addFailure(KW_NOT_ANALYZED);
                }
            }
            
            if (metricsBuildFailureCategory.getTotalCount() > 0){
                results.add(metricsBuildFailureCategory);
            }
        }
 
        return results;
    }
 
}
