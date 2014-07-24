/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.model;

import com.nokia.ci.ejb.util.Order;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 *
 * @author hhellgre
 */
public class BaseLazyDataModel<T> extends LazyDataModel<T> {

    protected String convertSortField(String sortField) {
        int lastDot = sortField.lastIndexOf('.');
        if (lastDot + 1 < sortField.length()) {
            return sortField.substring(lastDot + 1);
        } else {
            return sortField;
        }
    }

    protected Order convertOrder(SortOrder sortOrder) {
        if (sortOrder.equals(SortOrder.ASCENDING)) {
            return Order.ASC;
        }
        return Order.DESC;
    }

    @Override
    public void setRowIndex(int rowIndex) {
        /*
         * THIS IS A PRIMEFACE BUG http://code.google.com/p/primefaces/issues/detail?id=1544
         * The following is in ancestor (LazyDataModel):
         * this.rowIndex = rowIndex == -1 ? rowIndex : (rowIndex % pageSize);
         */
        if (rowIndex == -1 || getPageSize() == 0) {
            super.setRowIndex(-1);
        } else {
            super.setRowIndex(rowIndex % getPageSize());
        }
    }
}
