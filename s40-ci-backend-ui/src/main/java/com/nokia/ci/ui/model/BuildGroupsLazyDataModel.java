/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.model;

import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.BuildGroupLoader;
import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.util.Order;
import com.nokia.ci.ui.util.ExtendedBuildUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
public class BuildGroupsLazyDataModel extends BaseLazyDataModel<ExtendedBuildGroup> {

    private static final Logger log = LoggerFactory.getLogger(BuildGroupsLazyDataModel.class);
    private BuildGroupLoader ejb;
    private BuildGroupEJB bgEJB;
    private Long dataId;
    static final int TIMEOUT = 7 * 1000;
    private int socketTimeout = TIMEOUT;
    private int connectionTimeout = TIMEOUT;

    public BuildGroupsLazyDataModel(BuildGroupLoader ejb, BuildGroupEJB bgEJB, Long dataId) {
        this(ejb, bgEJB, null, dataId);
    }

    public BuildGroupsLazyDataModel(BuildGroupLoader ejb, BuildGroupEJB bgEJB, SysConfigEJB sysConfigEJB, Long dataId) {
        super();
        this.ejb = ejb;
        this.dataId = dataId;
        this.bgEJB = bgEJB;
        if (sysConfigEJB != null) {
            connectionTimeout = sysConfigEJB.getValue(SysConfigKey.HTTP_CLIENT_CONNECTION_TIMEOUT, TIMEOUT);
            socketTimeout = sysConfigEJB.getValue(SysConfigKey.HTTP_CLIENT_SOCKET_TIMEOUT, TIMEOUT);
        }
    }

    @Override
    public List<ExtendedBuildGroup> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
        Order order = Order.DESC;
        String field = "startTime";
        if (sortField != null) {
            order = convertOrder(sortOrder);
            field = convertSortField(sortField);
        }

        List<BuildGroup> buildGroups = ejb.getBuildGroups(dataId, first, pageSize, field, order);

        // rowCount
        Long dataSize = ejb.getBuildGroupCount(dataId);
        setRowCount(dataSize.intValue());
        return createExtendedBuilds(buildGroups);
    }

    private List<ExtendedBuildGroup> createExtendedBuilds(List<BuildGroup> buildGroups) {
        List<ExtendedBuildGroup> ret = new ArrayList<ExtendedBuildGroup>();
        if (buildGroups == null) {
            return ret;
        }
        for (BuildGroup buildGroup : buildGroups) {
            List<Change> changes = loadChangesIfAny(buildGroup);
            ret.add(ExtendedBuildUtil.createExtendedBuild(buildGroup, changes, socketTimeout, connectionTimeout));
        }
        return ret;
    }

    private List<Change> loadChangesIfAny(BuildGroup buildGroup) {
        List<Change> changes = null;
        if (buildGroup != null) {
            changes = bgEJB.getChanges(buildGroup.getId());
        }
        if (changes == null || changes.isEmpty()) {
            log.debug("Could not find 'Changes' for buildgroup {}", buildGroup);
        }
        return changes;
    }
}
