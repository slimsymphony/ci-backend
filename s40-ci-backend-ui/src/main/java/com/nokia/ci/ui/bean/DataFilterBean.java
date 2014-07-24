/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.model.BaseEntity;
import java.util.List;
import javax.faces.bean.ViewScoped;

/**
 *
 * @author hhellgre
 */
@ViewScoped
public abstract class DataFilterBean<T extends BaseEntity> extends AbstractUIBaseBean {

    protected List<T> filteredValues;

    public List<T> getFilteredValues() {
        return filteredValues;
    }

    public void setFilteredValues(List<T> filteredValues) {
        this.filteredValues = filteredValues;
    }
}
