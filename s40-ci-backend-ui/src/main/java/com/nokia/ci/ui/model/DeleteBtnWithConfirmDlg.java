package com.nokia.ci.ui.model;

import org.primefaces.extensions.util.ComponentUtils;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 2/18/13 Time: 9:44 AM To change
 * this template use File | Settings | File Templates.
 */
@FacesComponent("deleteBtnWithConfirmDlg")
public class DeleteBtnWithConfirmDlg extends UINamingContainer {

    private UIComponent confirmDialog;
    private String confirmDlgWidgetId;

    @Override
    public void encodeBegin(FacesContext facesContext) throws IOException {
        confirmDlgWidgetId = "confirmDialog_" + UUID.randomUUID().toString().replace("-", "");
        confirmDialog.setId(confirmDlgWidgetId);
        super.encodeBegin(facesContext);
    }

    @Override
    public void encodeEnd(FacesContext facesContext) throws IOException {
        super.encodeEnd(facesContext);
        confirmDlgWidgetId = null;
    }

    public UIComponent getConfirmDialog() {
        return confirmDialog;
    }

    public void setConfirmDialog(UIComponent confirmDialog) {
        this.confirmDialog = confirmDialog;
    }

    public String getDlgWidgetVar() {
        return confirmDlgWidgetId;
    }

    public String getParentUpdate() {
        String update = (String) getAttributes().get("update");
        if (update.startsWith("@")) {
            return update;
        } else {
            return ":" + ComponentUtils.findComponentClientId(update);
        }
    }
}
