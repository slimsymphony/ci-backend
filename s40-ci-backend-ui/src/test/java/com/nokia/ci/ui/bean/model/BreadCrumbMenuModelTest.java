package com.nokia.ci.ui.bean.model;

import com.nokia.ci.ui.model.BreadCrumbMenuModel;
import com.nokia.ci.ui.model.CIMenuItem;
import com.nokia.ci.ui.model.ProjectsMenu;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link BreadCrumbMenuModel} class.
 * 
 * @author vrouvine
 */
public class BreadCrumbMenuModelTest {
    
    private static final Long PROJECT_ID = 1L;
    private static final Long VERIFICATION_ID = 2L;
    private static final Long BUILD_ID = 3L;
    
    private BreadCrumbMenuModel model;
    
    @Before
    public void setUp() {
        model = createProjectMenuModel(); 
    }
    
    @Test
    public void enableMenuEmptyModel() {
        model = new BreadCrumbMenuModel();
        Assert.assertFalse(model.enableMenu(ProjectsMenu.HOME, Long.MIN_VALUE, Long.MIN_VALUE, "Label"));
    }

    @Test
    public void enableProjectMenu() {
        enableInMenu(ProjectsMenu.PROJECT, PROJECT_ID, null, "X", true);
    }
    
    @Test
    public void enableVerificationMenu() {
        enableInMenu(ProjectsMenu.PROJECT, PROJECT_ID, null, "X", true);
        enableInMenu(ProjectsMenu.VERIFICATION, VERIFICATION_ID, PROJECT_ID, "Y", true);
    }
    
    @Test
    public void enableBuildMenu() {
        enableInMenu(ProjectsMenu.PROJECT, PROJECT_ID, null, "X", true);
        enableInMenu(ProjectsMenu.VERIFICATION, VERIFICATION_ID, PROJECT_ID, "Y", true);
        enableInMenu(ProjectsMenu.BUILD, BUILD_ID, VERIFICATION_ID, "Z", true);
    }
    
    @Test
    public void invalidParent() {
        enableInMenu(ProjectsMenu.PROJECT, PROJECT_ID, null, "X", true);
        enableInMenu(ProjectsMenu.VERIFICATION, VERIFICATION_ID, Long.MAX_VALUE, "Y", false);
    }
    
    private void enableInMenu(ProjectsMenu menu, Long entityId, Long parentEntityId, String name, boolean wantedValue) {
        boolean value = model.enableMenu(menu, entityId, parentEntityId, name);
        Assert.assertEquals(wantedValue, value);
        
        CIMenuItem item = (CIMenuItem) model.getContents().get(ProjectsMenu.indexOf(menu));
        Assert.assertEquals(String.format(menu.getBaseUrl(), entityId), item.getUrl());
        Assert.assertTrue(item.isRendered());
    }
    
    private BreadCrumbMenuModel createProjectMenuModel() {
        BreadCrumbMenuModel m = new BreadCrumbMenuModel();
        for(ProjectsMenu menu : ProjectsMenu.values()) {
            m.appendMenuItem(menu.toString(), menu.getBaseUrl(), menu.getLabelPrefix(), menu.hasParent());
        }
        return m;
    }
}
