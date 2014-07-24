/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.api.util;

import com.nokia.ci.client.model.MemConsumptionView;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.Component;
import com.nokia.ci.ejb.model.MemConsumption;
import com.nokia.ci.ejb.util.RelationUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author larryang
 */
public class MemConsumptionViewUtil {

    public static Map<Component, MemConsumption> transformMemConsumptions(List<MemConsumptionView> mcvs) {
        
        Map<Component, MemConsumption> compMemConsumptions = new HashMap();
        
        for (MemConsumptionView mcv : mcvs){
            
            Component component = new Component();
            component.setName(mcv.getComponentName());
            
            MemConsumption memConsumption = new MemConsumption();
            memConsumption.setComponent(component);
            memConsumption.setRam((float)mcv.getRam());
            memConsumption.setRom((float)mcv.getRom());
            
            component.getMemConsumptions().add(memConsumption);

            compMemConsumptions.put(component, memConsumption);
        }
        
        return compMemConsumptions;
    }
    
    public static boolean sanityCheck(List<MemConsumptionView> mcvs) {
        
        for (MemConsumptionView mcv : mcvs) {

            if (mcv.getComponentName() == null) {
                return false;
            }

            if (mcv.getRam() < 0) {
                return false;
            }
            
            if(mcv.getRom() < 0) {
                return false;
            }
        }

        return true;
    }
    
    public static void fillPersistentObjectsToMemConsumptions(Build build, List<Component> components, Map<Component, MemConsumption> componentConsumptions){
        
        for (Map.Entry<Component, MemConsumption> entry : componentConsumptions.entrySet()){

            for (Component component : components){
                if (entry.getValue().getComponent().getName().equals(component.getName())){
                    RelationUtil.partialRelate(component, entry.getValue());
                }
            }
        }
    }
    
}
