/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.bean.comp;

import java.io.IOException;
import javax.faces.component.FacesComponent;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;

/**
 *
 * @author hhellgre
 */
@FacesComponent(value = "AutoLinkOutputText")
public class AutoLinkOutputText extends UIComponentBase implements NamingContainer {

    private String outputValue;

    @Override
    public String getFamily() {
        return UINamingContainer.COMPONENT_FAMILY;
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        outputValue = (String) getAttributes().get("value");

        if (outputValue == null) {
            outputValue = "";
            return;
        }

        // < and > characters
        outputValue = outputValue.replaceAll("<", "&lt;");
        outputValue = outputValue.replaceAll(">", "&gt;");
        // http/https/ftp/file
        outputValue = outputValue.replaceAll("\\b(https?|ftp|file)://\\S+\\b", "<a href=\"$0\" target=\"_blank\">$0</a>");
        // www.
        outputValue = outputValue.replaceAll("(^|[^/])(www.[\\S]+(\\b|$))", "$1<a href=\"http://$2\" target=\"_blank\">$2</a>");
        // email addresses
        outputValue = outputValue.replaceAll("([a-zA-Z0-9_.]+@[a-zA-Z_]+?\\.[a-zA-Z]{2,6})", "<a href=\"mailto:$1\">$1</a>");

        super.encodeBegin(context);
    }

    public String getOutputValue() {
        return outputValue;
    }

    public void setOutputValue(String outputValue) {
        this.outputValue = outputValue;
    }
}
