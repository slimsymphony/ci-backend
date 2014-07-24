/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.metrics;

/**
 *
 * @author larryang
 */
public enum MetricsLevel {
    BUILD, VERIFICATION, SYSTEM;
    
    @Override
    public String toString(){
        
        if (this == BUILD){
            return "build";
        }else if (this == SYSTEM){
            return "system";
        }else{
            return "verification";
        }
    }
}