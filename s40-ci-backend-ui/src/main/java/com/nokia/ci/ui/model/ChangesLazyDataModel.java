/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.model;

import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.util.Order;
import java.util.List;
import java.util.Map;
import org.primefaces.model.SortOrder;

/**
 *
 * @author hhellgre
 */
public class ChangesLazyDataModel extends BaseLazyDataModel<Change> {

    private Long dataId;
    private SysUserEJB ejb;

    public ChangesLazyDataModel(SysUserEJB ejb, Long dataId) {
        super();
        this.dataId = dataId;
        this.ejb = ejb;
    }

    @Override
    public List<Change> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
        Order order = Order.DESC;
        String field = "commitTime";
        if (sortField != null) {
            order = convertOrder(sortOrder);
            field = convertSortField(sortField);
        }

        List<Change> changes = ejb.getChanges(dataId, first, pageSize, field, order);

        // rowCount
        Long dataSize = ejb.getChangeCount(dataId);
        setRowCount(dataSize.intValue());
        return changes;
    }
}
