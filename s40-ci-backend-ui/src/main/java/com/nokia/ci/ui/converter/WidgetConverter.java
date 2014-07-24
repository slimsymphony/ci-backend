/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.converter;

import com.nokia.ci.ejb.model.Widget;
import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author hhellgre
 */
@FacesConverter("WidgetConverter")
public class WidgetConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        List<Widget> myWidgets = (List<Widget>) context.getApplication().evaluateExpressionGet(context, "#{userSettingsBean.myWidgets}", List.class);
        for (Widget widget : myWidgets) {
            if (widget.getIdentifier().equals(value)) {
                return widget;
            }
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((Widget) value).getIdentifier();
    }
}
