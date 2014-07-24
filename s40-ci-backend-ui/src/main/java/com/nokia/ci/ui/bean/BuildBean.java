package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.BuildEJB;
import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.BuildVerificationConfEJB;
import com.nokia.ci.ejb.ChangeEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.VerificationEJB;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.BranchType;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildCustomParameter;
import com.nokia.ci.ejb.model.BuildEvent;
import com.nokia.ci.ejb.model.BuildEventPhase;
import com.nokia.ci.ejb.model.BuildFailure;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildPhase;
import com.nokia.ci.ejb.model.BuildResultDetailsParam;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.model.BuildVerificationConf;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.ChangeFile;
import com.nokia.ci.ejb.model.ChangeFileType;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.model.Verification;
import com.nokia.ci.ui.exception.QueryParamException;
import com.nokia.ci.ejb.model.VerificationFailureReasonSeverity;
import com.nokia.ci.ui.jenkins.BuildDetailResolver;
import com.nokia.ci.ui.jenkins.JSONChange;
import com.nokia.ci.ui.jenkins.JSONParam;
import com.nokia.ci.ui.model.BuildEventTiming;
import com.nokia.ci.ui.model.ClassificationStatus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for Build view.
 *
 * @author vrouvine
 */
@Named
@ViewScoped
public class BuildBean extends AbstractUIBaseBean {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(BuildBean.class);
    private BuildGroup buildGroup;
    private Job job;
    private Project project;
    private List<JSONChange> changes;
    private List<JSONParam> parameters;
    private List<Build> buildGroupVerifications;
    private List<Change> gitGerritChanges;
    private List<BuildResultDetailsParam> buildResultDetailsParams;
    private List<BuildEventTiming> buildEventTimings;
    @Inject
    private SysConfigEJB sysConfigEJB;
    @Inject
    private BuildEJB buildEJB;
    @Inject
    private BuildGroupEJB buildGroupEJB;
    @Inject
    private BuildVerificationConfEJB buildVerificationConfEJB;
    @Inject
    private JobEJB jobEJB;
    @Inject
    private ChangeEJB changeEJB;
    @Inject
    private HttpSessionBean httpSessionBean;
    @Inject
    private VerificationEJB verificationEJB;
    @Inject
    private ProjectEJB projectEJB;
    private Map<String, String> verificationDescriptions;
    static final int TIMEOUT = 7 * 1000;
    private int socketTimeout = TIMEOUT;
    private boolean pollStop = false;
    private String patchsetRevision;
    private String startNodeBuildIds;
    private Map<Build, String> formattedCustomParameters;
    private Map<Build, String> childBuildIdStrings;
    private Map<Change, List<ChangeFile>> addedFiles;
    private Map<Change, List<ChangeFile>> modifiedFiles;
    private Map<Change, List<ChangeFile>> renamedFiles;
    private Map<Change, List<ChangeFile>> removedFiles;
    private Map<Change, List<Change>> parentChanges;
    private Map<Change, List<Change>> childChanges;
    private Map<Build, List<BuildFailure>> buildFailures;
    private Map<Build, ClassificationStatus> buildClassificationStatuses;

    @Override
    protected void init() throws BackendAppException, QueryParamException {
        long startTime = System.currentTimeMillis();
        log.debug("Initializing...");

        addedFiles = new HashMap<Change, List<ChangeFile>>();
        modifiedFiles = new HashMap<Change, List<ChangeFile>>();
        renamedFiles = new HashMap<Change, List<ChangeFile>>();
        removedFiles = new HashMap<Change, List<ChangeFile>>();
        parentChanges = new HashMap<Change, List<Change>>();
        childChanges = new HashMap<Change, List<Change>>();
        buildFailures = new HashMap<Build, List<BuildFailure>>();
        buildClassificationStatuses = new HashMap<Build, ClassificationStatus>();

        patchsetRevision = "Loading changes, please wait...";
        verificationDescriptions = new HashMap<String, String>();

        String retriggered = getQueryParam("retriggered");
        if (retriggered != null) {
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Retriggered!", "This build has been retriggered by parent build " + retriggered);
        }

        String buildGroupId = getMandatoryQueryParam("buildId");

        log.debug("Finding build group with id {}", buildGroupId);

        refreshBuildGroup(Long.parseLong(buildGroupId));
        job = jobEJB.readSecure(buildGroup.getJob().getId());

        project = projectEJB.read(job.getProjectId());

        initBuildGroupVerifications();

        socketTimeout = sysConfigEJB.getValue(SysConfigKey.HTTP_CLIENT_SOCKET_TIMEOUT, TIMEOUT);

        log.debug("Initializing finished in {}ms", System.currentTimeMillis() - startTime);
    }

    public void initChanges() throws NotFoundException {
        if (buildGroup != null) {
            gitGerritChanges = buildGroupEJB.getChanges(buildGroup.getId());

            // Fallback to Jenkins changes
            if (gitGerritChanges.isEmpty()) {
                if (buildGroup.getUrl() == null) {
                    return;
                }
                BuildDetailResolver bdr = new BuildDetailResolver(buildGroup.getUrl(), socketTimeout, socketTimeout);
                changes = bdr.fetchChanges();
            } else {
                Collections.sort(gitGerritChanges);
                patchsetRevision = buildGroup.getGerritPatchSetRevision();
            }
        }
    }

    public List<BuildEventTiming> getBuildEventTimings() {
        return buildEventTimings;
    }

    public void retrigger() {
        if (!httpSessionBean.isLoggedIn()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Retrigger failed.", "You have to login first!");
            return;
        }

        if (!isOwnBuild()) {
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Retrigger failed.", "You do not have priviliges to retrigger this build!");
            return;
        }

        List<Change> chs = buildGroupEJB.getChanges(buildGroup.getId());

        if (!chs.isEmpty()) {
            try {
                buildGroupEJB.retrigger(buildGroup.getId());
                addMessage(FacesMessage.SEVERITY_INFO, "Retriggered!", "The build was retriggered and will start in few seconds");
                return;
            } catch (Exception ex) {
            }
        } else {
            log.warn("Retriggering failed. No changes found for build {}", buildGroup);
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Retrigger failed.", "No recorded changes found. Please, use Jenkins for retriggering the change if possible.");
            return;
        }

        log.warn("Retriggering failed for build {}", buildGroup);
        addMessage(FacesMessage.SEVERITY_WARN,
                "Retrigger failed.", "There was a problem in the system. Please try again or contact support.");
    }

    public Boolean isOwnBuild() {
        if (httpSessionBean.hasAdminAccessToProject(buildGroup.getProjectId())) {
            return true;
        }

        if (job == null || job.getBranch() == null) {
            return false;
        }

        BranchType type = job.getBranch().getType();

        if (type != BranchType.SINGLE_COMMIT && type != BranchType.TOOLBOX && type != BranchType.DRAFT) {
            return false;
        }

        if (buildGroup == null) {
            return false;
        }

        List<Change> chs = null;
        if (buildGroup != null) {
            chs = buildGroupEJB.getChanges(buildGroup.getId());
        }

        if (chs == null || chs.isEmpty()) {
            return false;
        }

        for (Change c : chs) {
            if (c.getAuthorEmail().equals(httpSessionBean.getSysUserEmail())) {
                return true;
            }
        }

        return false;
    }

    public boolean getPollStop() {
        return this.pollStop;
    }

    public void setPollStop(boolean pollStop) {
        this.pollStop = pollStop;
    }

    public String getBuildChildIds(Build build) {
        return childBuildIdStrings.get(build);
    }

    public String getStartNodeBuildIds() {
        return startNodeBuildIds;
    }

    public String getPatchsetRevision() {
        return patchsetRevision;
    }

    public void setPatchsetRevision(String patchsetRevision) {
        this.patchsetRevision = patchsetRevision;
    }

    public List<ChangeFile> getAddedFiles(Change change) {
        if (change != null && !addedFiles.containsKey(change)) {
            initChangeFiles(change);
        }

        return addedFiles.get(change);
    }

    public List<ChangeFile> getModifiedFiles(Change change) {
        if (change != null && !modifiedFiles.containsKey(change)) {
            initChangeFiles(change);
        }
        return modifiedFiles.get(change);
    }

    public List<ChangeFile> getRemovedFiles(Change change) {
        if (change != null && !removedFiles.containsKey(change)) {
            initChangeFiles(change);
        }
        return removedFiles.get(change);
    }

    public List<ChangeFile> getRenamedFiles(Change change) {
        if (change != null && !renamedFiles.containsKey(change)) {
            initChangeFiles(change);
        }
        return renamedFiles.get(change);
    }

    public List<BuildResultDetailsParam> getBuildResultDetailsParams() {
        return buildResultDetailsParams;
    }

    public List<Change> getParentChanges(Change change) {
        List<Change> parents = parentChanges.get(change);
        if (change != null && parents == null) {
            parents = changeEJB.getParentChanges(change.getId());
            parentChanges.put(change, parents);
        }
        return parents;
    }

    public List<Change> getChildChanges(Change change) {
        List<Change> childs = childChanges.get(change);
        if (change != null && childs == null) {
            childs = changeEJB.getChildChanges(change.getId());
            childChanges.put(change, childs);
        }
        return childs;
    }

    public void release() throws BackendAppException {
        try {
            buildGroupEJB.release(buildGroup.getId());
        } catch (BackendAppException ex) {
            log.info("Releasing failed. Cause: {}", ex.getMessage());
            return;
        }

        updateBuildGroup();
    }

    public boolean showReleaseButton() {
        // Only admin can release for now.
        if (!httpSessionBean.isAdmin()) {
            return false;
        }

        // Only for master branch builds.
        if (!isMasterBuild()) {
            return false;
        }

        // Release only once.
        if (isReleased()) {
            return false;
        }

        // Phase must be FINISHED.
        if (buildGroup.getPhase() != BuildPhase.FINISHED) {
            return false;
        }

        // Status must be SUCCESS.
        if (buildGroup.getStatus() != BuildStatus.SUCCESS) {
            return false;
        }

        // All checks passed.
        return true;
    }

    public boolean isReleased() {
        if (buildGroup != null) {
            BuildGroup bg;

            try {
                bg = buildGroupEJB.readSecure(buildGroup.getId());
            } catch (NotFoundException ex) {
                log.warn("Could not read build group for build {}", buildGroup);
                return false;
            } catch (BackendAppException ex) {
                log.warn("Could not read build group for build {}. Not authorized.", buildGroup);
                return false;
            }

            if (bg.getRelease() != null) {
                return true;
            }
        }

        return false;
    }

    public boolean isMasterBuild() {
        if (job != null && job.getBranch() != null
                && job.getBranch().getType() == BranchType.MASTER) {
            return true;
        }

        return false;
    }

    public Date getReleaseDate() {
        if (buildGroup != null
                && buildGroup.getRelease() != null) {
            return buildGroup.getRelease().getReleaseTime();
        }

        return null;
    }

    public String getCustomParameterString(Build b) {
        return formattedCustomParameters.get(b);
    }

    public BuildGroup getBuildGroup() {
        return buildGroup;
    }

    public List<Change> getGitGerritChanges() {
        return gitGerritChanges;
    }

    public void setGitGerritChanges(List<Change> gitGerritChanges) {
        this.gitGerritChanges = gitGerritChanges;
    }

    public List<Build> getBuildGroupVerifications() {
        return buildGroupVerifications;
    }

    public void setBuildGroup(BuildGroup buildGroup) {
        this.buildGroup = buildGroup;
    }

    public void updateBuildGroup() throws BackendAppException {
        if (buildGroup == null) {
            return;
        }
        long startTime = System.currentTimeMillis();
        log.debug("Refreshing builds for {}", buildGroup);

        refreshBuildGroup(buildGroup.getId());
        refreshBuildGroupVerifications();
        setPollStop(buildGroup.getPhase() == BuildPhase.FINISHED);

        log.debug("Refreshed builds in {}ms", System.currentTimeMillis() - startTime);
    }

    public List<JSONChange> getChanges() {
        return changes;
    }

    public Job getJob() {
        return job;
    }

    public Project getProject() {
        return project;
    }

    public List<JSONParam> getParameters() {
        return parameters;
    }

    public Map<String, String> getVerificationDescriptions() {
        return verificationDescriptions;
    }

    private void initChangeFiles(Change change) {
        if (change == null) {
            return;
        }

        List<ChangeFile> added = new ArrayList<ChangeFile>();
        List<ChangeFile> modified = new ArrayList<ChangeFile>();
        List<ChangeFile> renamed = new ArrayList<ChangeFile>();
        List<ChangeFile> removed = new ArrayList<ChangeFile>();

        List<ChangeFile> changeFiles = changeEJB.getChangeFiles(change.getId());

        for (ChangeFile f : changeFiles) {
            if (f.getFileType() == ChangeFileType.ADDED || f.getFileType() == ChangeFileType.COPIED) {
                added.add(f);
            } else if (f.getFileType() == ChangeFileType.MODIFIED || f.getFileType() == ChangeFileType.REWRITE) {
                modified.add(f);
            } else if (f.getFileType() == ChangeFileType.REMOVED) {
                removed.add(f);
            } else if (f.getFileType() == ChangeFileType.RENAMED) {
                renamed.add(f);
            }
        }

        addedFiles.put(change, added);
        modifiedFiles.put(change, modified);
        renamedFiles.put(change, renamed);
        removedFiles.put(change, removed);
    }

    private String formatCustomParameterString(Build b) {
        if (b == null) {
            return "";
        }

        BuildVerificationConf buildVerificationConf = b.getBuildVerificationConf();
        if (buildVerificationConf == null) {
            return "";
        }

        List<BuildCustomParameter> customParameters = buildVerificationConf.getCustomParameters();
        if (customParameters == null || customParameters.isEmpty()) {
            return "";
        }

        long startTime = System.currentTimeMillis();
        log.debug("Formating custom parameters for {}", b);

        ListIterator<BuildCustomParameter> listIterator = customParameters.listIterator();
        StringBuilder sb = new StringBuilder();
        while (listIterator.hasNext()) {
            BuildCustomParameter param = listIterator.next();
            if (listIterator.previousIndex() > 0 && sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(param.getParamKey()).append("=").append(param.getParamValue());
        }
        String s = sb.toString();
        log.debug("Formating done in {}ms", System.currentTimeMillis() - startTime);
        return s;
    }

    private void refreshBuildGroup(Long buildGroupId) throws BackendAppException {
        long startTime = System.currentTimeMillis();
        log.debug("Refreshing build for {}", buildGroup);

        buildGroup = buildGroupEJB.readSecure(buildGroupId);
        buildGroup.setRelease(buildGroupEJB.getRelease(buildGroup.getId()));

        log.debug("Refreshed build in {}", System.currentTimeMillis() - startTime);
    }

    private void initBuildVerificationConfs(Build b) {
        if (b == null) {
            log.warn("Build is null for {}!", buildGroup);
            return;
        }
        BuildVerificationConf buildVerificationConf = b.getBuildVerificationConf();
        if (buildVerificationConf == null) {
            log.warn("BuildVerificationConf is null for {}", b);
            return;
        }
        List<BuildCustomParameter> customParameters = buildVerificationConfEJB.getCustomParameters(buildVerificationConf.getId());
        b.getBuildVerificationConf().setCustomParameters(customParameters);

        Verification v = verificationEJB.getVerificationByUuid(buildVerificationConf.getVerificationUuid());
        if (v != null && StringUtils.isNotEmpty(v.getDescription())) {
            verificationDescriptions.put(v.getName(), v.getDescription());
        }
    }

    private void initBuildGroupVerifications() throws NotFoundException {

        buildEventTimings = new ArrayList<BuildEventTiming>();
        formattedCustomParameters = new HashMap<Build, String>();
        childBuildIdStrings = new HashMap<Build, String>();
        startNodeBuildIds = "";

        long startTime = System.currentTimeMillis();
        log.debug("Init builds from build group {}", buildGroup);

        refreshBuildGroupVerifications();

        List<Build> startNodes = new ArrayList<Build>();

        for (Build b : buildGroupVerifications) {
            if (b.getStartNode() != null && b.getStartNode() == true) {
                startNodes.add(b);
            }

            initBuildVerificationConfs(b);

            String customParamStr = formatCustomParameterString(b);
            formattedCustomParameters.put(b, customParamStr);

            List<Build> children = buildEJB.getChildBuilds(b.getId());
            String output = createBuildIdString(children);

            childBuildIdStrings.put(b, output);
        }

        startNodeBuildIds = createBuildIdString(startNodes);

        log.debug("Initialized builds from build group in {}ms", System.currentTimeMillis() - startTime);
    }

    private void refreshBuildGroupVerifications() {
        buildGroupVerifications = new ArrayList<Build>();
        buildGroupVerifications = buildGroupEJB.getBuilds(buildGroup.getId());

        long maxSCMSpan = 0;
        long maxBuildSpan = 0;
        long maxTestSpan = 0;
        long maxTotalSpan = 0;

        for (Build b : buildGroupVerifications) {
            BuildEventTiming curBuildEventTiming = getBuildEventTiming(b);
            buildEventTimings.add(curBuildEventTiming);

            maxSCMSpan = Math.max(curBuildEventTiming.getScmTimeSpan(), maxSCMSpan);
            maxBuildSpan = Math.max(curBuildEventTiming.getBuildTimeSpan(), maxBuildSpan);
            maxTestSpan = Math.max(curBuildEventTiming.getTestTimeSpan(), maxTestSpan);
            maxTotalSpan = Math.max(curBuildEventTiming.getTotalTimeSpan(), maxTotalSpan);
            
            refreshBuildClassificationStatus(b);
        }

        for (BuildEventTiming curBuildEventTiming : buildEventTimings) {
            curBuildEventTiming.setBuildPercentage((int) Math.round((double) curBuildEventTiming.getBuildTimeSpan() / (double) maxBuildSpan * 100));
            curBuildEventTiming.setScmPercentage((int) Math.round((double) curBuildEventTiming.getScmTimeSpan() / (double) maxSCMSpan * 100));
            curBuildEventTiming.setTestPercentage((int) Math.round((double) curBuildEventTiming.getTestTimeSpan() / (double) maxTestSpan * 100));
            curBuildEventTiming.setTotalPercentage((int) Math.round((double) curBuildEventTiming.getTotalTimeSpan() / (double) maxTotalSpan * 100));
        }
    }

    private String createBuildIdString(List<Build> builds) {
        String output = "0";
        if (builds.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(builds.get(0).getId());
            if (builds.size() > 1) {
                for (int j = 1; j < builds.size(); j++) {
                    sb.append(",");
                    sb.append(builds.get(j).getId());
                }
            }
            output = sb.toString();
        }
        return output;
    }
    
    protected void refreshBuildClassificationStatus(Build build) {
        
        try{
            if (build == null){
                log.warn("Input parameter build, for refreshBuildClassificationStatus, is null");
                return;
            }
            
            boolean isClassifiable = true;
            boolean classifiableChecked = false;
            
            if (buildClassificationStatuses.containsKey(build)){
                ClassificationStatus classificationStatus = buildClassificationStatuses.get(build);
                
                if (classificationStatus.isClassifiedOk()){
                    //The classification status of this build has been in terminate status, no need to update again.
                    return;
                }else if (classificationStatus.isClassified()){
                    //It must be classifiable.
                    isClassifiable = true;
                    classifiableChecked = true;
                }
            }
            
            if (!classifiableChecked){
                isClassifiable = buildEJB.isClassifiable(build.getId());
            }
            boolean classified = true;
            boolean classifiedOk = true;

            if (!isClassifiable){
                classifiedOk = false;
            }

            List<BuildFailure> fails = getBuildFailures(build);

            if (fails.isEmpty()) {
                classified = false;
                classifiedOk = false;
            }

            for (BuildFailure f : fails) {
                // No reason for some failure, not classified if not success!
                if (f.getFailureReason() == null) {
                    classified = false;
                    classifiedOk = false;
                }else{
                    // indication of error in application behaviour.
                    if (f.getFailureReason().getSeverity() == null) {
                        log.error("build failure reason {} does not contain severity", f.getFailureReason().getId());
                        classifiedOk = false;
                    }

                    // Some reason is blocking, not classified even build success!
                    if (f.getFailureReason().getSeverity() == VerificationFailureReasonSeverity.BLOCKING) {
                        classifiedOk = false;
                    }
                }
            }
            
            //Sync up status
            if (!isClassifiable){
                classified = false;
                classifiedOk = false;
            }

            ClassificationStatus classificationStatus = new ClassificationStatus(isClassifiable, classified, classifiedOk);

            //create or update build classification status.
            buildClassificationStatuses.put(build, classificationStatus);

        } catch (NotFoundException nfex) {
            log.warn("NotFoundException during refreshBuildClassificationStatus for build {}, {}", build.getId(), nfex);
        } catch (Exception ex){
            log.warn("Exception during refreshBuildClassificationStatus for build {}, {}", build.getId(), ex);
        }
    }

    public boolean isClassifiable(Build build){
        
        if (build == null){
            log.warn("Input parameter build is null with isClassifiable");
            return false;
        }
        
        if (buildClassificationStatuses.containsKey(build)){
            ClassificationStatus classificationStatus = buildClassificationStatuses.get(build);
            return classificationStatus.isClassifiable();
        }else{
            log.warn("Build {} is not found when getting isClassifiable.", build.getId());
            return false;
        }
    }
    
    public boolean classified(Build build){

        if (build == null){
            log.warn("Input parameter build is null with classified");
            return false;
        }
        
        if (buildClassificationStatuses.containsKey(build)){
            ClassificationStatus classificationStatus = buildClassificationStatuses.get(build);
            return classificationStatus.isClassified();
        }else{
            log.warn("Build {} is not found when getting classified.", build.getId());
            return false;
        }
    }
            
    public boolean classifiedOk(Build build){

        if (build == null){
            log.warn("Input parameter build is null with classifiedOk");
            return false;
        }
        
        if (buildClassificationStatuses.containsKey(build)){
            ClassificationStatus classificationStatus = buildClassificationStatuses.get(build);
            return classificationStatus.isClassifiedOk();
        }else{
            log.warn("Build {} is not found when getting classifiedOk.", build.getId());
            return false;
        }
    }

    private List<BuildFailure> getBuildFailures(Build build) {
        List<BuildFailure> fails = new ArrayList<BuildFailure>();
        try {
            if (buildFailures.containsKey(build)) {
                fails = buildFailures.get(build);
            } else {
                fails = buildEJB.getBuildFailures(build.getId());
                buildFailures.put(build, fails);
            }
        } catch (NotFoundException ex) {
            log.warn(ex.getMessage());
        }
        return fails;
    }

    public void initBuildResultDetailsParams() {
        buildResultDetailsParams = new ArrayList<BuildResultDetailsParam>();
        if (buildGroup == null) {
            return;
        }
        long startTime = System.currentTimeMillis();
        log.debug("Init build result details params from build group {}", buildGroup);

        buildResultDetailsParams = buildGroupEJB.getBuildResultDetailsParams(buildGroup.getId());
        log.debug("Initialized build result details params from build group in {}ms", System.currentTimeMillis() - startTime);

    }

    private BuildEventTiming getBuildEventTiming(Build b) {

        List<BuildEvent> buildEvents = buildEJB.getBuildEvents(b.getId());

        BuildEventTiming buildEventTiming = new BuildEventTiming();

        for (BuildEvent buildEvent : buildEvents) {
            if (buildEvent.getName().equalsIgnoreCase("SCM")) {
                if (buildEvent.getPhase() == BuildEventPhase.START) {
                    buildEventTiming.setScmStartTimestamp(buildEvent.getTimestamp());
                } else if (buildEvent.getPhase() == BuildEventPhase.END) {
                    buildEventTiming.setScmEndTimestamp(buildEvent.getTimestamp());
                }
            } else if (buildEvent.getName().equalsIgnoreCase("BUILD")) {
                if (buildEvent.getPhase() == BuildEventPhase.START) {
                    buildEventTiming.setBuildStartTimestamp(buildEvent.getTimestamp());
                } else if (buildEvent.getPhase() == BuildEventPhase.END) {
                    buildEventTiming.setBuildEndTimestamp(buildEvent.getTimestamp());
                }
            } else if (buildEvent.getName().equalsIgnoreCase("TEST")) {
                if (buildEvent.getPhase() == BuildEventPhase.START) {
                    buildEventTiming.setTestStartTimestamp(buildEvent.getTimestamp());
                } else if (buildEvent.getPhase() == BuildEventPhase.END) {
                    buildEventTiming.setTestEndTimestamp(buildEvent.getTimestamp());
                }
            }
        }

        buildEventTiming.setExecutor(b.getExecutor());
        buildEventTiming.setJobName(
                (b.getBuildVerificationConf() == null || b.getBuildVerificationConf().getVerificationDisplayName() == null
                || b.getBuildVerificationConf().getVerificationDisplayName().equals(""))
                ? b.getJobDisplayName() : b.getBuildVerificationConf().getVerificationDisplayName());

        return buildEventTiming;

    }

}
