/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.converter;

import java.security.GeneralSecurityException;

import com.nokia.ci.ejb.util.LDAPUser;
import com.nokia.ci.ejb.util.LDAPUtil;
import com.unboundid.ldap.sdk.LDAPException;
import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author hhellgre
 */
@RequestScoped
@FacesConverter("LDAPUserConverter")
public class LDAPUserConverter implements Converter {
    
    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component, String submittedValue) {               
        if (submittedValue.trim().equals("")) {
            return null;
        }
        
        LDAPUser user = null;
        
        try {
            LDAPUtil util = new LDAPUtil();
            user = util.getByUsername(submittedValue);
        } catch (GeneralSecurityException e) {
            return null;
        } catch(LDAPException e) {
            return null;
        }
        
        return user;
    }
    
    @Override
    public String getAsString(FacesContext facesContext, UIComponent component, Object value) {  
        if(value == null || value.equals("")) {
            return null;
        }
        return ((LDAPUser)value).getUsername();
    }
    
}
