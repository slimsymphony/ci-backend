/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.jenkins;

/**
 *
 * @author jajuutin
 */
public class JenkinsClientException extends Exception {

    public JenkinsClientException(String msg) {
        super(msg);
    }

    public JenkinsClientException(Throwable cause) {
        super(cause);
    }
}
