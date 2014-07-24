/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.model;

import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ejb.util.Order;
import java.util.List;
import java.util.Map;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
public class UsersLazyDataModel extends BaseLazyDataModel<SysUser> {

    private static final Logger log = LoggerFactory.getLogger(UsersLazyDataModel.class);
    private SysUserEJB ejb;

    public UsersLazyDataModel(SysUserEJB ejb) {
        super();
        this.ejb = ejb;
    }

    @Override
    public List<SysUser> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
        Order order = Order.DESC;
        String field = "loginName";
        if (sortField != null) {
            order = convertOrder(sortOrder);
            field = convertSortField(sortField);
        }

        List<SysUser> users = ejb.getSysUsers(first, pageSize, field, order, filters);

        // rowCount
        setRowCount(ejb.readAll().size());
        return users;
    }
}
