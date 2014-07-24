package com.nokia.ci.ejb.util;

import com.google.gson.Gson;
import com.nokia.ci.ejb.gerrit.model.CurrentPatchSet;
import com.nokia.ci.ejb.gerrit.model.GerritDetail;
import com.nokia.ci.ejb.gerrit.model.GerritFile;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.ChangeFile;
import com.nokia.ci.ejb.model.ChangeFileType;
import com.nokia.ci.ejb.model.GerritFileType;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper methods for handling gerrit data.
 *
 * @author vrouvine
 */
public class GerritUtils {

    private static Logger log = LoggerFactory.getLogger(GerritUtils.class);
    private static final long TIMESTAMP_FIX_VALUE = 315532800000L; // 01.01.1980 00:00:00

    public static GerritDetail parseGerritDetail(String line) {

        Gson gson = new Gson();
        // Convert received line to UTF-8
        String item;
        try {
            item = new String(line.getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.warn("Encoding format was incorrect! " + e.getMessage());
            item = line;
        }
        return GerritUtils.fixTimestamps(gson.fromJson(item, GerritDetail.class));
    }

    public static GerritDetail fixTimestamps(GerritDetail detail) {

        if (detail.getCreatedOn() != null && detail.getCreatedOn() < TIMESTAMP_FIX_VALUE) {
            detail.setCreatedOn(detail.getCreatedOn() * 1000);
        }

        if (detail.getLastUpdated() != null && detail.getLastUpdated() < TIMESTAMP_FIX_VALUE) {
            detail.setLastUpdated(detail.getLastUpdated() * 1000);
        }

        if (detail.getCurrentPatchSet() != null && detail.getCurrentPatchSet().getCreatedOn() != null
                && detail.getCurrentPatchSet().getCreatedOn() < TIMESTAMP_FIX_VALUE) {
            detail.getCurrentPatchSet().setCreatedOn(detail.getCurrentPatchSet().getCreatedOn() * 1000);
        }

        if (detail.getPatchSet() != null && detail.getPatchSet().getCreatedOn() != null
                && detail.getPatchSet().getCreatedOn() < TIMESTAMP_FIX_VALUE) {
            detail.getPatchSet().setCreatedOn(detail.getPatchSet().getCreatedOn() * 1000);
        }

        return detail;
    }

    /**
     * Transforms all files from {@link CurrentPatchSet} to list of
     * {@link ChangeFile} entities and relates them to given {@link Change}
     * entity.
     *
     * @param change {@link Change} entity where files related to
     * @param patchSet Gerrit patchset
     * @return List of {@link ChangeFile} entities related to given change.
     */
    public static List<ChangeFile> getChangeFiles(Change change, CurrentPatchSet patchSet) {
        List<ChangeFile> changeFiles = new ArrayList<ChangeFile>();
        if (change != null && patchSet != null && patchSet.getFiles() != null) {
            for (GerritFile file : patchSet.getFiles()) {
                ChangeFile changeFile = new ChangeFile();
                changeFile.setFilePath(file.getFile());
                if (file.getType().equals(GerritFileType.ADDED.toString())) {
                    changeFile.setFileType(ChangeFileType.ADDED);
                } else if (file.getType().equals(GerritFileType.MODIFIED.toString())) {
                    changeFile.setFileType(ChangeFileType.MODIFIED);
                } else if (file.getType().equals(GerritFileType.DELETED.toString())) {
                    changeFile.setFileType(ChangeFileType.REMOVED);
                } else if (file.getType().equals(GerritFileType.RENAMED.toString())) {
                    changeFile.setFileType(ChangeFileType.RENAMED);
                } else if (file.getType().equals(GerritFileType.COPIED.toString())) {
                    changeFile.setFileType(ChangeFileType.COPIED);
                } else if (file.getType().equals(GerritFileType.REWRITE.toString())) {
                    changeFile.setFileType(ChangeFileType.REWRITE);
                }
                changeFile.setChange(change);
                changeFiles.add(changeFile);
            }
        }
        return changeFiles;
    }
}
