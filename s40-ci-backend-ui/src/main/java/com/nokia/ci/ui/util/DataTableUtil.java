/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.util;

import java.util.Map;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.html.HtmlGraphicImage;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import org.primefaces.component.column.Column;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.extensions.component.tooltip.Tooltip;

/**
 *
 * @author hhellgre
 */
public class DataTableUtil {

    public static Column createColumn(DataTable t, String header) {
        return DataTableUtil.createColumn(t, header, null);
    }

    public static Column createColumn(DataTable t, String header, String valueExpr) {
        return DataTableUtil.createColumn(t, header, valueExpr, Boolean.FALSE);
    }

    public static Column createColumn(DataTable t, String header, String valueExpr, Boolean sort) {
        Column col = new Column();
        HtmlOutputText head = new HtmlOutputText();
        head.setValue(header);
        DataTableUtil.linkComponents(col, head);
        col.getFacets().put("header", head);
        DataTableUtil.linkComponents(t, col);

        if (valueExpr != null) {
            FacesContext fc = FacesContext.getCurrentInstance();

            ELContext ctx = fc.getELContext();
            ExpressionFactory factory = fc.getApplication().getExpressionFactory();
            ValueExpression expr = factory.createValueExpression(ctx, valueExpr, String.class);

            HtmlOutputText columnText = (HtmlOutputText) fc.getApplication().createComponent(HtmlOutputText.COMPONENT_TYPE);
            columnText.setValueExpression("value", expr);
            DataTableUtil.linkComponents(col, columnText);

            if (sort.equals(Boolean.TRUE)) {
                col.setValueExpression("sortBy", expr);
            }

        }

        return col;
    }

    public static HtmlOutputLink createActionLink(String target, String img) {
        return DataTableUtil.createActionLink(target, img, null, null);
    }

    public static HtmlOutputLink createActionLink(String target, String img, String tooltip) {
        return DataTableUtil.createActionLink(target, img, null, tooltip);
    }

    public static HtmlOutputLink createActionLink(String target, String img, Map<String, String> params) {
        return DataTableUtil.createActionLink(target, img, params, null);
    }

    public static HtmlOutputLink createActionLink(String target, String img, Map<String, String> params, String tooltip) {
        HtmlOutputLink link = new HtmlOutputLink();
        link.setValue(target);

        if (img != null) {
            HtmlGraphicImage image = new HtmlGraphicImage();
            image.setStyleClass("actionButton");
            image.setValue(img);

            DataTableUtil.linkComponents(link, image);

            if (tooltip != null) {
                Tooltip tip = new Tooltip();
                tip.setMyPosition("top right");
                tip.setAtPosition("bottom center");
                tip.setFor(image.getId());
            }
        }

        if (params != null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            ELContext ctx = fc.getELContext();
            ExpressionFactory factory = fc.getApplication().getExpressionFactory();

            for (Map.Entry<String, String> pair : params.entrySet()) {
                UIParameter param = new UIParameter();
                param.setName(pair.getKey());
                ValueExpression expr = factory.createValueExpression(ctx, pair.getValue(), String.class);
                param.setValueExpression("value", expr);
                DataTableUtil.linkComponents(link, param);
            }
        }

        return link;
    }

    public static void linkComponents(UIComponent parent, UIComponent child) {
        parent.getChildren().add(child);
    }
}
