package com.nokia.ci.ui.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.faces.component.UIComponent;
import org.primefaces.component.menuitem.MenuItem;
import org.primefaces.component.separator.Separator;
import org.primefaces.component.submenu.Submenu;
import org.primefaces.model.MenuModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MenuModel implementation for bread crumb menu.
 *
 * @author vrouvine
 */
public class BreadCrumbMenuModel implements MenuModel, Serializable {

    private static final Logger log = LoggerFactory.getLogger(BreadCrumbMenuModel.class);
    private List<UIComponent> contents;

    public BreadCrumbMenuModel() {
        contents = new ArrayList<UIComponent>();
    }

    /**
     * Creates new menu item and appends it last of the bread crumb menu.
     *
     * @param id ID of menu item
     * @param url URL of menu item
     * @param label Label of menu item
     * @param parent {@code true} if menu item has parent item
     */
    public void appendMenuItem(String id, String url, String label, boolean parent) {
        CIMenuItem item = new CIMenuItem();
        item.setParent(parent);
        item.setId(id);
        item.setValue(label);
        item.setUrl(url);
        item.setAjax(false);
        item.setRendered(false);
        addMenuItem(item);
    }

    /**
     * Enables given menu item in menu content. Method validates also integrity
     * of menu hierarchy and returns {@code false} if menu model should be
     * re-build with correct relations.
     *
     * @param menu Menu enumeration
     * @param entityId Menu item entity id.
     * @param parentEntityId Parent entity id of this menu item.
     * @param label Display label for menu item.
     * @return {@code true} if menu enabled successfully and menu hierarchy is
     * valid.
     */
    public boolean enableMenu(ProjectsMenu menu, Long entityId, Long parentEntityId, String label) {
        boolean visible = true;
        for (UIComponent comp : contents) {
            CIMenuItem item = (CIMenuItem) comp;
            item.setRendered(visible);
            if (item.getId().equals(menu.toString())) {
                item.setEntityId(entityId);
                item.setParentEntityId(parentEntityId);
                item.setUrl(String.format(menu.getBaseUrl(), entityId));
                item.setValue(String.format(menu.getLabelPrefix(), label));
                visible = false;
            }
        }
        return validate();
    }

    /**
     * Resets menu model content.
     */
    public void reset() {
        contents.clear();
    }

    @Override
    public List<UIComponent> getContents() {
        return contents;
    }

    @Override
    public void addSubmenu(Submenu subMenu) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addMenuItem(MenuItem menuItem) {
        contents.add(menuItem);
    }
    
    @Override
    public void addSeparator(Separator separator) {
        contents.add(separator);
    }

    /**
     * Validates current menu contents integrity.
     *
     * @return {@code true} if contents is valid.
     */
    public boolean validate() {
        log.debug("Validating menu integrity...");
        int start = contents.size();
        if (start < 1) {
            log.debug("Menu does not have enough items!");
            return false;
        }
        Long lastParentEntityId = null;
        ListIterator<UIComponent> it = contents.listIterator();
        while (it.hasNext()) {
            CIMenuItem item = (CIMenuItem) it.next();
            if (!item.isRendered()) {
                break;
            }

            if (item.hasParent() && item.getParentEntityId() == null) {
                log.debug("Menu item {} should have parent but does not have parent entity id");
                return false;
            }

            if (item.hasParent() && !item.getParentEntityId().equals(lastParentEntityId)) {
                log.debug("Parent id [{}] is not matching item's parent entity id [{}]", lastParentEntityId, item.getParentEntityId());
                return false;
            }
            lastParentEntityId = item.getEntityId();
        }
        return true;
    }
}
