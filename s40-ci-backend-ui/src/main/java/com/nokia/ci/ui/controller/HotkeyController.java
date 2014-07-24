/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.controller;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;

/**
 *
 * @author hhellgre
 */
@Named
public class HotkeyController {
    
    public String gotoPage(String page) {
        String action = page;
        action += page.contains("?") ? "&" : "?";
        action += "faces-redirect=true";
        return action;
    }
}
