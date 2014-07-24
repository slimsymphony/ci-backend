/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.cicontroller;

/**
 *
 * @author jajuutin
 */
public class CIControllerException extends Exception {
    
    public CIControllerException(String msg) {
        super(msg);
    }
    
    public CIControllerException(Throwable cause) {
        super(cause);
    }
}
