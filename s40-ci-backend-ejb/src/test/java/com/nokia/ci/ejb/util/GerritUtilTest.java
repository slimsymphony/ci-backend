package com.nokia.ci.ejb.util;

import com.nokia.ci.ejb.gerrit.model.CurrentPatchSet;
import com.nokia.ci.ejb.gerrit.model.GerritFile;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.ChangeFile;
import com.nokia.ci.ejb.model.ChangeFileType;
import com.nokia.ci.ejb.model.GerritFileType;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link GerritUtils}
 *
 * @author vrouvine
 */
public class GerritUtilTest {
    
    @Test
    public void getChangeFilesParamVariations() throws Exception {
        Assert.assertTrue("List should be empty!", GerritUtils.getChangeFiles(null, null).isEmpty());
        Assert.assertTrue("List should be empty!", GerritUtils.getChangeFiles(new Change(), null).isEmpty());
        Assert.assertTrue("List should be empty!", GerritUtils.getChangeFiles(null, new CurrentPatchSet()).isEmpty());
        Assert.assertTrue("List should be empty!", GerritUtils.getChangeFiles(new Change(), new CurrentPatchSet()).isEmpty());
        
        CurrentPatchSet currentPatchSet = new CurrentPatchSet();
        currentPatchSet.setFiles(new ArrayList<GerritFile>());
        Assert.assertTrue("List should be empty!", GerritUtils.getChangeFiles(new Change(), currentPatchSet).isEmpty());
    }

    @Test
    public void getChangedFiles() throws Exception {
        Change change = new Change();
        CurrentPatchSet currentPatchSet = new CurrentPatchSet();
        currentPatchSet.setFiles(createGerritFiles());
        List<ChangeFile> changeFiles = GerritUtils.getChangeFiles(change, currentPatchSet);
        Assert.assertNotNull("Change files should not be null!", changeFiles);
        Assert.assertEquals("Change files size does not match!", ChangeFileType.values().length, changeFiles.size());
        ListIterator<ChangeFile> listIterator = changeFiles.listIterator();
        while (listIterator.hasNext()) {
            ChangeFile cf = listIterator.next();
            GerritFile gf = currentPatchSet.getFiles().get(listIterator.previousIndex());
            Assert.assertNotNull("Change file should have type!", cf.getFileType());
            Assert.assertNotNull("Change file should have path!", cf.getFilePath());
            Assert.assertEquals("Type should match!", gf.getType(), cf.getFileType() == ChangeFileType.REMOVED ? GerritFileType.DELETED.toString() : cf.getFileType().toString());
            Assert.assertEquals("Path should match!", gf.getFile(), cf.getFilePath());
            Assert.assertEquals("Change does not match in change file!", change, cf.getChange());
        }
    }

    private List<GerritFile> createGerritFiles() {
        List<GerritFile> files = new ArrayList<GerritFile>();
        for (GerritFileType type : GerritFileType.values()) {
            GerritFile file = new GerritFile();
            file.setFile("/" + type.toString() + "/" + type.ordinal());
            file.setType(type.toString());
            files.add(file);
        }
        return files;
    }
}
