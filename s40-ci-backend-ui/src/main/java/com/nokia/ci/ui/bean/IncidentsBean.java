package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.IncidentEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Incident;
import com.nokia.ci.ejb.model.IncidentType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.security.RolesAllowed;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 3/7/13 Time: 3:31 PM To change
 * this template use File | Settings | File Templates.
 */
@Named
@ViewScoped
public class IncidentsBean extends AbstractUIBaseBean {

    private static Logger log = LoggerFactory.getLogger(IncidentsBean.class);
    private Map<IncidentType, List<Incident>> uncheckedIncidents = new HashMap<IncidentType, List<Incident>>();
    private Map<IncidentType, List<Incident>> checkedIncidents = new HashMap<IncidentType, List<Incident>>();
    private List<IncidentType> uncheckedIncidentTypes = new ArrayList<IncidentType>();
    private List<IncidentType> checkedIncidentTypes = new ArrayList<IncidentType>();
    @Inject
    private IncidentEJB incidentEJB;
    @Inject
    private HttpSessionBean httpSessionBean;

    @Override
    protected void init() {
        initIncidents();
    }

    private void initIncidents() {
        for (IncidentType type : IncidentType.values()) {
            List<Incident> unchecked = incidentEJB.getUncheckedIncidentsByType(type);
            if (!unchecked.isEmpty()) {
                uncheckedIncidentTypes.add(type);
                uncheckedIncidents.put(type, unchecked);
            }

            List<Incident> checked = incidentEJB.getCheckedIncidentsByType(type);
            if (!checked.isEmpty()) {
                checkedIncidentTypes.add(type);
                checkedIncidents.put(type, checked);
            }
        }
    }

    public List<IncidentType> getUncheckedIncidentTypes() {
        return uncheckedIncidentTypes;
    }

    public List<IncidentType> getCheckedIncidentTypes() {
        return checkedIncidentTypes;
    }

    public Map<IncidentType, List<Incident>> getUncheckedIncidents() {
        return uncheckedIncidents;
    }

    public Map<IncidentType, List<Incident>> getCheckedIncidents() {
        return checkedIncidents;
    }

    @RolesAllowed("SYSTEM_ADMIN")
    public void markAsChecked(Incident incident) {
        log.info("Marking incident {} as checked", incident);
        try {
            incident.setCheckTime(new Date());
            incident.setCheckUser(httpSessionBean.getSysUser().getLoginName());
            for (Map.Entry pairs : uncheckedIncidents.entrySet()) {
                List<Incident> incidents = (List<Incident>) pairs.getValue();
                if (incidents == null) {
                    continue;
                }

                if (incidents.contains(incident)) {
                    incidents.remove(incident);
                    checkedIncidents.get((IncidentType) pairs.getKey()).add(incident);
                    break;
                }
            }
            incidentEJB.update(incident);
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "Incident was marked as checked.");
        } catch (NotFoundException ex) {
            log.warn("Marking Incident {} failed! Cause: {}", incident, ex.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Marking incident failed!", "Selected incident could not be marked!");
        }
    }
}
