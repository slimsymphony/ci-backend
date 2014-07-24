/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.gerrit.model;

import java.util.Comparator;

/**
 *
 * @author miikka
 */
public class GerritDetailTimeComparer implements Comparator<GerritDetail> {

    @Override
    public int compare(GerritDetail detail1, GerritDetail detail2) {
        if (detail1.getLastUpdated() > detail2.getLastUpdated()) {
            return 1;
        } else if (detail1.getLastUpdated() < detail2.getLastUpdated()) {
            return -1;
        } else {
            return 0;
        }

    }
}