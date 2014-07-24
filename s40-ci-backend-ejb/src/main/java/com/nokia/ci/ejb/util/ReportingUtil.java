package com.nokia.ci.ejb.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildResultDetailsParam;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.VerificationCardinality;

/**
*
* @author ttyppo
*/

public class ReportingUtil {
    
    private static final int JOB_DISPLAY_NAME_MAX_LENGTH = 40; // Must be more than 3
    private static final int VERIFICATION_DISPLAY_NAME_MAX_LENGTH = 25; // Must be more than 3
    private static final int PRODUCT_DISPLAY_NAME_MAX_LENGTH = 20; // Must be more than 3
    private static final int DURATION_MAX_LENGTH = 20; // Must be more than 3
    private static final int BUILD_RESULT_MAX_LENGTH = 10; // Must be more than 3
    private static final int AUTHOR_NAME_MAX_LENGTH = 20; // Must be more than 3
    private static final int SUBJECT_MAX_LENGTH = 40; // Must be more than 3
    private static final int RESULT_DETAIL_DISPLAY_NAME_MAX_LENGTH = 25; // Must be more than 3
    private static final int RESULT_DETAIL_VERIFICATION_DISPLAY_NAME_MAX_LENGTH = 20; // Must be more than 3
    private static final int RESULT_DETAIL_PRODUCT_DISPLAY_NAME_MAX_LENGTH = 15; // Must be more than 3
    private static final String RESULT_ITEM_SEPARATOR = " ";
    
    public static List<String> formatBuildResults(Build build) {
        List<Build> builds = new ArrayList<Build>();
        builds.add(build);
        return formatBuildResults(builds);
    }
    
    public static List<String> formatBuildResults(List<Build> childBuilds) {
        
        List<String> results = new ArrayList<String>();    
        for (Build childBuild : childBuilds) {
            StringBuilder buildResultString = new StringBuilder();
            if (childBuild.getBuildVerificationConf() == null ||
                    childBuild.getBuildVerificationConf().getVerificationDisplayName() == null ||
                    childBuild.getBuildVerificationConf().getVerificationDisplayName().isEmpty()) {
                // Add Job Display Name
                addJobDisplayName(buildResultString, childBuild);
            } else {
                // Add Build Cardinality
                addCardinality(buildResultString, childBuild);
                buildResultString.append(RESULT_ITEM_SEPARATOR);

                // Add Verification Name
                addVerificationDisplayName(buildResultString, childBuild);

                // Add Product Name
                if (childBuild.getBuildVerificationConf().getProductDisplayName() != null) {
                    buildResultString.append(RESULT_ITEM_SEPARATOR);
                    addProductDisplayName(buildResultString, childBuild);
                }
            }
            buildResultString.append(RESULT_ITEM_SEPARATOR);

            // Add Build Result
            addBuildResult(buildResultString, childBuild);
            buildResultString.append(RESULT_ITEM_SEPARATOR);

            // Add Build Duration
            addBuildDuration(buildResultString, childBuild);

            results.add(buildResultString.toString());
        }
        
        // Sort Results
        Collections.sort(results);
        
        return results;
    }

    public static List<String> formatChanges(List<Change> changes) {
        List<String> results = new ArrayList<String>();
        for (Change change : changes) {
            // Add Commit ID
            StringBuilder changeString = new StringBuilder(change.getCommitId());
            changeString.append(RESULT_ITEM_SEPARATOR);

            // Add Subject
            addSubject(changeString, change);
            changeString.append(RESULT_ITEM_SEPARATOR);

            // Add Author Name
            addAuthorName(changeString, change);

            results.add(changeString.toString());
        }

        // Sort Results
        Collections.sort(results);
        
        return results;
    }
    
    public static List<String> formatBuildResultDetailsParams(List<BuildResultDetailsParam> details) {
        List<String> results = new ArrayList<String>();
        for (BuildResultDetailsParam detail : details) {
            StringBuilder resultDetailsString = new StringBuilder();
            
            // Add Verification Display Name
            addResultDetailVerificationName(resultDetailsString, detail);
            resultDetailsString.append(RESULT_ITEM_SEPARATOR);

            // Add Product Display Name
            addResultDetailProductName(resultDetailsString, detail);
            resultDetailsString.append(RESULT_ITEM_SEPARATOR);

            // Add Display Name
            addResultDetailDisplayName(resultDetailsString, detail);
            resultDetailsString.append(RESULT_ITEM_SEPARATOR);
            
            //Add Parameter Value
            resultDetailsString.append(detail.getParamValue());

            results.add(resultDetailsString.toString());
        }

        // Sort Results
        Collections.sort(results);
      
        return results;
    }
    
    
    public static String appendAuthors(String recipients, BuildGroup buildGroup) {
        
        return appendAuthors(recipients, buildGroup.getChanges());
    }
    
    public static String appendAuthors(String recipients, List<Change> changes) {
        
        if (recipients == null) {
            recipients = "";
        }
        if (changes == null) {
            return recipients;
        }
        StringBuilder builder = new StringBuilder(recipients);
        for (Change change : changes) {
            if (change.getAuthorEmail() == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(change.getAuthorEmail());
        }
        return builder.toString();
    }
    
    private static void addBuildDuration(StringBuilder buildResultString, Build childBuild) {
        if(childBuild.getStartTime() == null || childBuild.getEndTime() == null) {
            return;
        }
        
        long duration = childBuild.getEndTime().getTime() - childBuild.getStartTime().getTime();
        String buildDuration = StringUtils.rightPad(DurationFormatUtils.formatDurationWords(duration, 
                true, false), DURATION_MAX_LENGTH, RESULT_ITEM_SEPARATOR);
        buildDuration = StringUtils.abbreviate(buildDuration, PRODUCT_DISPLAY_NAME_MAX_LENGTH);
        buildResultString.append(buildDuration);
    }

    private static void addBuildResult(StringBuilder buildResultString, Build childBuild) {
        String buildResult = StringUtils.rightPad(childBuild.getStatus().toString(), 
                BUILD_RESULT_MAX_LENGTH, RESULT_ITEM_SEPARATOR);
        buildResult = StringUtils.abbreviate(buildResult, BUILD_RESULT_MAX_LENGTH);
        buildResultString.append(buildResult);
    }

    private static void addProductDisplayName(StringBuilder buildResultString, Build childBuild) {
        String productName = StringUtils.rightPad(childBuild.getBuildVerificationConf().getProductDisplayName(), 
                PRODUCT_DISPLAY_NAME_MAX_LENGTH, RESULT_ITEM_SEPARATOR);
        productName = StringUtils.abbreviate(productName, PRODUCT_DISPLAY_NAME_MAX_LENGTH);
        buildResultString.append(productName);
    }

    private static void addVerificationDisplayName(StringBuilder buildResultString, Build childBuild) {
        String verificationName = StringUtils.rightPad(childBuild.getBuildVerificationConf().getVerificationDisplayName(), 
                VERIFICATION_DISPLAY_NAME_MAX_LENGTH, RESULT_ITEM_SEPARATOR);
        verificationName = StringUtils.abbreviate(verificationName, VERIFICATION_DISPLAY_NAME_MAX_LENGTH);
        buildResultString.append(verificationName);
    }

    private static void addCardinality(StringBuilder buildResultString, Build childBuild) {
        VerificationCardinality cardinality = childBuild.getBuildVerificationConf().getCardinality(); 
        buildResultString.append(cardinality == VerificationCardinality.MANDATORY ? "[M]" : "[O]");
    }

    private static void addJobDisplayName(StringBuilder buildResultString, Build childBuild) {
        String jobDisplayName = StringUtils.rightPad(childBuild.getJobDisplayName(), 
                JOB_DISPLAY_NAME_MAX_LENGTH, RESULT_ITEM_SEPARATOR);
        jobDisplayName = StringUtils.abbreviate(jobDisplayName, JOB_DISPLAY_NAME_MAX_LENGTH);
        buildResultString.append(jobDisplayName);
    }

    private static void addAuthorName(StringBuilder changeString, Change change) {
        String authorName = StringUtils.rightPad(change.getAuthorName(), 
                AUTHOR_NAME_MAX_LENGTH, RESULT_ITEM_SEPARATOR);
        authorName = StringUtils.abbreviate(authorName, AUTHOR_NAME_MAX_LENGTH);
        changeString.append(authorName);
    }

    private static void addSubject(StringBuilder changeString, Change change) {
        String subject = StringUtils.rightPad(change.getSubject(), 
                SUBJECT_MAX_LENGTH, RESULT_ITEM_SEPARATOR);
        subject = StringUtils.abbreviate(subject, SUBJECT_MAX_LENGTH);
        changeString.append(subject);
    }
    
    private static void addResultDetailDisplayName(StringBuilder resultDetailsString, BuildResultDetailsParam detail) {
        String displayName = StringUtils.rightPad(detail.getDisplayName(), 
                RESULT_DETAIL_DISPLAY_NAME_MAX_LENGTH, RESULT_ITEM_SEPARATOR);
        displayName = StringUtils.abbreviate(displayName, RESULT_DETAIL_DISPLAY_NAME_MAX_LENGTH);
        resultDetailsString.append(displayName);
    }
    
    private static void addResultDetailVerificationName(StringBuilder resultDetailsString, BuildResultDetailsParam detail) {
        if (detail.getBuildVerificationConf() != null) {
            String displayName = StringUtils.rightPad(detail.getBuildVerificationConf().getVerificationDisplayName(), 
                    RESULT_DETAIL_VERIFICATION_DISPLAY_NAME_MAX_LENGTH, RESULT_ITEM_SEPARATOR);
            displayName = StringUtils.abbreviate(displayName, RESULT_DETAIL_VERIFICATION_DISPLAY_NAME_MAX_LENGTH);
            resultDetailsString.append(displayName);
        }
    }

    private static void addResultDetailProductName(StringBuilder resultDetailsString, BuildResultDetailsParam detail) {
        if (detail.getBuildVerificationConf() != null) {
            String displayName = StringUtils.rightPad(detail.getBuildVerificationConf().getProductDisplayName(), 
                    RESULT_DETAIL_PRODUCT_DISPLAY_NAME_MAX_LENGTH, RESULT_ITEM_SEPARATOR);
            displayName = StringUtils.abbreviate(displayName, RESULT_DETAIL_PRODUCT_DISPLAY_NAME_MAX_LENGTH);
            resultDetailsString.append(displayName);
        }
    }

}
