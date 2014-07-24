package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.BuildEJB;
import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.metrics.BuildMetricsEJB;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.Component;
import java.util.ArrayList;
import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean base class for component based build trend metrics.
 *
 * @author larryang
 */
public abstract class BuildMetricsComponentBasedTrendBeanBase extends MetricsLineChartBeanBase {

    private Logger log = LoggerFactory.getLogger(BuildMetricsComponentBasedTrendBeanBase.class);

    @Inject
    private BuildMetricsEJB buildMetricsEJB;
    @Inject
    private BuildEJB buildEJB;
    @Inject
    private BuildGroupEJB buildGroupEJB;
    
    private List<String> components;
    private String selectedComponent;
    
    @Override
    public void init() {
        super.init();
        
        initProperties();
        
        getBuildIdFromRequest();
        initComponents();
        
        if(isRendered() && getChart() != null) {
            String component = getQueryParam("componentname");
            if(component != null && !component.equals("")) {
                setSelectedComponent(component);
            }
            updateDataModel();
        }
    }
    
    protected void initProperties(){
        
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
            Build build = buildEJB.read(id);
            openBuild(build.getBuildGroup().getId());
        }catch (NotFoundException e){
            log.error("Build not found {}", id);
        }
    }

    public List<String> getComponents() {
        return components;
    }

    public void setComponents(List<String> components) {
        this.components = components;
    }

    public String getSelectedComponent() {
        return selectedComponent;
    }

    public void setSelectedComponent(String selectedComponent) {
        this.selectedComponent = selectedComponent;
    }

    public List<String> completeComponents(String query) {
        
        if (components == null){
            FacesContext context = FacesContext.getCurrentInstance();
            this.setBuildId(Long.parseLong(UIComponent.getCurrentComponent(context).getAttributes().get("buildId").toString()));
            initComponents();
        }
                
        return components;
    }
    
    public void initComponents(){
        
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
        
        components = new ArrayList<String>();

        for (Component curComponent : componentList){
            components.add(curComponent.getName());
        }
        
        if (components.size() > 0){
            selectedComponent = components.get(0) ;
        }
    }
    
    @Override
    protected String createPermalinkURL() {
        if (selectedComponent != null && !selectedComponent.equals("")){
            return (super.createPermalinkURL() + selectedComponent + "/");
        }else{
            return super.createPermalinkURL();
        }
    }
    
}