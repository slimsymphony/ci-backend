/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.api.util;

import com.nokia.ci.client.model.BuildFailureView;
import com.nokia.ci.ejb.model.BuildFailure;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author hhellgre
 */
public class BuildFailureViewUtilTest {

    @Test
    public void sanityCheckOkTest() {
        List<BuildFailureView> views = new ArrayList<BuildFailureView>();
        for (int i = 0; i < 5; i++) {
            BuildFailureView view = new BuildFailureView();
            view.setTestcaseName("Test case " + i);
            views.add(view);
        }

        boolean result = BuildFailureViewUtil.sanityCheck(views);

        Assert.assertEquals(true, result);
    }

    @Test
    public void sanityCheckFailTest() {
        List<BuildFailureView> views = new ArrayList<BuildFailureView>();
        for (int i = 0; i < 5; i++) {
            BuildFailureView view = new BuildFailureView();
            views.add(view);
        }

        boolean result = BuildFailureViewUtil.sanityCheck(views);

        Assert.assertEquals(false, result);
    }

    @Test
    public void transformBuildFailureTest() {
        List<BuildFailureView> views = new ArrayList<BuildFailureView>();
        for (int i = 0; i < 5; i++) {
            BuildFailureView view = new BuildFailureView();
            view.setMessage("Message " + i);
            view.setType("WARNING");
            view.setRelativePath("/tmp/");
            view.setTestcaseName("Test case " + i);
            views.add(view);
        }

        List<BuildFailure> failures = BuildFailureViewUtil.transformBuildFailures(views);

        Assert.assertEquals(views.size(), failures.size());
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(views.get(i).getMessage(), failures.get(i).getMessage());
            Assert.assertEquals(views.get(i).getType(), failures.get(i).getType());
            Assert.assertEquals(views.get(i).getRelativePath(), failures.get(i).getRelativePath());
            Assert.assertEquals(views.get(i).getTestcaseName(), failures.get(i).getTestcaseName());
        }
    }
}
