package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.BuildEJB;
import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.metrics.BuildMetricsEJB;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean base class for component based build status metrics.
 * @author larryang
 */
public abstract class BuildMetricsComponentBasedStatusBeanBase extends MetricsLineChartBeanBase {
    
    private Logger log = LoggerFactory.getLogger(BuildMetricsComponentBasedStatusBeanBase.class);
    @Inject
    private BuildEJB buildEJB;
    @Inject
    private BuildGroupEJB buildGroupEJB;
    @Inject
    private BuildMetricsEJB buildMetricsEJB;
    private List<String> selectedComponents;
    private Map<String,String> components;

    @Override
    public void init() {
        super.init();
        
        initProperties();
        
        getBuildIdFromRequest();
        initComponents();
        
        if(isRendered() && getChart() != null) {
            String componentsStr = getQueryParam("componentname");
            selectedComponents = new ArrayList<String>();
            for (String curComponentStr : componentsStr.split(",")){
                if (!curComponentStr.equals("null")) {
                    selectedComponents.add(curComponentStr);
                }
            }
            updateDataModel();
        }
    }
    
    protected void initProperties(){
        
    }
    
    public void initComponents(){
        
        components = new TreeMap<String,String>();
        
        if (getBuildId() == null){
            return;
        }
        
        Build build = null;
        
        try{
            build = buildEJB.read(getBuildId());
        }catch (NotFoundException e){
            log.error("Build not found {}", getBuildId());
            return;
        }
        
        List<Component> componentList = buildGroupEJB.getComponents(build.getBuildGroup().getId());
        
        for (Component curComponent : componentList){
            components.put(curComponent.getName(), curComponent.getName());
        }
    }
    
    @Override
    protected String createPermalinkURL() {
        String permalinkURL = super.createPermalinkURL();
        String componentsStr = "";
        
        for (int i=0; i<selectedComponents.size(); i++){
            componentsStr += selectedComponents.get(i);
            if (i < selectedComponents.size()-1){
                componentsStr += ",";
            }
        }
        
        if (componentsStr.equals("")){
            componentsStr = "null";
        }
        
        permalinkURL += componentsStr + "/";
        
        return permalinkURL;
    }

    public Map<String, String> getComponents() {
        
        if (components == null || components.isEmpty()){
            FacesContext context = FacesContext.getCurrentInstance();
            setBuildId(Long.parseLong(UIComponent.getCurrentCompositeComponent(context).getAttributes().get("buildId").toString()));
            initComponents();
        }
        
        return components;
    }

    public void setComponents(Map<String, String> components) {
        this.components = components;
    }

    public List<String> getSelectedComponents() {
        return selectedComponents;
    }

    public void setSelectedComponents(List<String> selectedComponents) {
        this.selectedComponents = selectedComponents;
    }

    public BuildEJB getBuildEJB() {
        return buildEJB;
    }

    public void setBuildEJB(BuildEJB buildEJB) {
        this.buildEJB = buildEJB;
    }

    public BuildGroupEJB getBuildGroupEJB() {
        return buildGroupEJB;
    }

    public void setBuildGroupEJB(BuildGroupEJB buildGroupEJB) {
        this.buildGroupEJB = buildGroupEJB;
    }

    public BuildMetricsEJB getBuildMetricsEJB() {
        return buildMetricsEJB;
    }

    public void setBuildMetricsEJB(BuildMetricsEJB buildMetricsEJB) {
        this.buildMetricsEJB = buildMetricsEJB;
    }
    
    @Override
    protected void open(Long id) {
        try{
            Build build = getBuildEJB().read(id);
            openBuild(build.getBuildGroup().getId());
        }catch (NotFoundException e){
            log.error("Build not found {}", id);
        }
    }
}
