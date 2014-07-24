/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.api.util;

import com.nokia.ci.client.model.BuildEventView;
import com.nokia.ci.ejb.model.BuildEvent;
import com.nokia.ci.ejb.model.BuildEventPhase;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jajuutin
 */
public class BuildEventViewUtil {

    public static List<BuildEvent> transformBuildEvents(List<BuildEventView> bevs) {
        List<BuildEvent> buildEvents = new ArrayList<BuildEvent>();

        for (BuildEventView bev : bevs) {
            BuildEvent be = new BuildEvent();
            be.setName(bev.getName());
            be.setPhase(transformPhase(bev.getPhase()));
            be.setDescription(bev.getDescription());
            be.setTimestamp(bev.getTimestamp());
            buildEvents.add(be);
        }

        return buildEvents;
    }

    public static boolean sanityCheck(List<BuildEventView> buildEventViews) {
        for (BuildEventView bev : buildEventViews) {
            // Description is allowed to be null.

            if (bev.getName() == null) {
                return false;
            }

            if (bev.getPhase() == null) {
                return false;
            }
            
            if(BuildEventViewUtil.transformPhase(bev.getPhase()) == null) {
                return false;
            }

            if (bev.getTimestamp() == null || bev.getTimestamp() < 0) {
                return false;
            }
        }

        return true;
    }
    
    private static BuildEventPhase transformPhase(String viewPhase) {
        BuildEventPhase result = null;

        if (viewPhase != null) {
            if (viewPhase.equals(BuildEventPhase.START.toString())) {
                result = BuildEventPhase.START;
            } else if (viewPhase.equals(BuildEventPhase.END.toString())) {
                result = BuildEventPhase.END;
            }
        }

        return result;
    }    
}
