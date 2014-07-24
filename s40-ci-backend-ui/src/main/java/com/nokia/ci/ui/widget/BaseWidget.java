/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.widget;

import com.nokia.ci.ejb.model.Widget;
import com.nokia.ci.ejb.model.WidgetSetting;
import com.nokia.ci.ejb.util.RelationUtil;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;
import org.primefaces.component.behavior.ajax.AjaxBehavior;
import org.primefaces.component.behavior.ajax.AjaxBehaviorListenerImpl;
import org.primefaces.component.panel.Panel;

/**
 *
 * @author hhellgre
 */
public abstract class BaseWidget implements Serializable {

    private String header;
    private String identifier;
    private Panel panel;
    private Boolean minimized;
    private Boolean closable;
    private Boolean toggleable;
    protected Widget persistentWidget;
    protected Map<String, String> settings = new HashMap<String, String>();
    protected String valueExpr;

    public BaseWidget() {
    }

    public BaseWidget(Long id, String header) {
        persistentWidget = new Widget();
        persistentWidget.setHeader(header);
        persistentWidget.setClassName(getClass().getName());

        WidgetSetting idSetting = new WidgetSetting();
        idSetting.setSettingKey("id");
        idSetting.setSettingValue(id.toString());
        RelationUtil.relate(persistentWidget, idSetting);
    }

    public void init(Widget w) {
        FacesContext fc = FacesContext.getCurrentInstance();
        Application application = fc.getApplication();
        panel = (Panel) application.createComponent(fc, "org.primefaces.component.Panel", "org.primefaces.component.PanelRenderer");

        this.persistentWidget = w;
        this.identifier = w.getIdentifier();
        this.header = w.getHeader();
        this.closable = true;
        this.toggleable = true;
        this.minimized = (w.getMinimized() == null ? false : w.getMinimized());
    }
    
    public void addSettingItem(String key, String value){
 
        WidgetSetting newSetting = new WidgetSetting();
        newSetting.setSettingKey(key);
        newSetting.setSettingValue(value);
        RelationUtil.relate(persistentWidget, newSetting);
    }

    public Widget getPersistentWidget() {
        return persistentWidget;
    }

    public void setPersistentWidget(Widget persistentWidget) {
        this.persistentWidget = persistentWidget;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public Panel getPanel() {
        return panel;
    }

    public Boolean getMinimized() {
        return minimized;
    }

    public void setMinimized(Boolean minimized) {
        this.minimized = minimized;
    }

    public Boolean getClosable() {
        return closable;
    }

    public void setClosable(Boolean closable) {
        this.closable = closable;
    }

    public Boolean getToggleable() {
        return toggleable;
    }

    public void setToggleable(Boolean toggleable) {
        this.toggleable = toggleable;
    }

    public void addToggleBehavior(String valueExpr) {
        addAjaxBehavior("toggle", valueExpr, String.class, new Class[0]);
    }

    public void addCloseBehavior(String valueExpr) {
        addAjaxBehavior("close", valueExpr, String.class, new Class[0]);
    }

    public String getValueExpr() {
        return valueExpr;
    }

    public void setValueExpr(String valueExpr) {
        this.valueExpr = valueExpr;
    }

    private void addAjaxBehavior(String ajaxCall, String valueExpr, Class returnVal, Class params[]) {
        if (panel == null) {
            return;
        }

        AjaxBehavior ab = new AjaxBehavior();
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExpressionFactory ef = ctx.getApplication().getExpressionFactory();
        MethodExpression me = ef.createMethodExpression(ctx.getELContext(), valueExpr, returnVal, params);
        ab.addAjaxBehaviorListener(new AjaxBehaviorListenerImpl(me, me));
        ab.setProcess("@this");
        panel.addClientBehavior(ajaxCall, ab);
    }

    public void addCompositeComponent(UIComponent parent, String libraryName, String resourceName, String id, Map<String, String> valueExpressions) {
        FacesContext context = FacesContext.getCurrentInstance();
        Application application = context.getApplication();
        FaceletContext faceletContext = (FaceletContext) context.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);

        Resource resource = application.getResourceHandler().createResource(resourceName, libraryName);
        if (resource == null) {
            return;
        }

        UIComponent composite = application.createComponent(context, resource);
        composite.setId(id);

        if (valueExpressions != null && !valueExpressions.isEmpty()) {
            ExpressionFactory factory = application.getExpressionFactory();
            ELContext ctx = context.getELContext();
            for (Map.Entry<String, String> entry : valueExpressions.entrySet()) {
                ValueExpression expr = factory.createValueExpression(ctx, entry.getValue(), Object.class);
                composite.setValueExpression(entry.getKey(), expr);
            }
        }

        UIComponent implementation = application.createComponent(UIPanel.COMPONENT_TYPE);
        implementation.setRendererType("javax.faces.Group");
        composite.getFacets().put(UIComponent.COMPOSITE_FACET_NAME, implementation);

        parent.getChildren().add(composite);
        parent.pushComponentToEL(context, composite);
        try {
            faceletContext.includeFacelet(implementation, resource.getURL());
        } catch (IOException e) {
            throw new FacesException(e);
        } finally {
            parent.popComponentFromEL(context);
        }
    }

    public void render() {
        if (panel == null) {
            return;
        }
        panel.setClosable(this.closable);
        panel.setToggleable(this.toggleable);
        panel.setCollapsed(this.minimized);
        panel.setVisible(true);
        panel.setRendered(true);
        panel.setId(identifier);
        panel.setHeader(header);
    }

    protected ValueExpression createValueExpression(String valueExpression, Class<?> valueType) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        return facesContext.getApplication().getExpressionFactory().createValueExpression(
                facesContext.getELContext(), valueExpression, valueType);
    }
}
