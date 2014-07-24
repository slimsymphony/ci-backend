package com.nokia.ci.ui.model;

import com.nokia.ci.ejb.model.BaseEntity;
import java.util.List;
import javax.faces.model.ListDataModel;
import org.primefaces.model.SelectableDataModel;

/**
 * Generic selectable list data model for {@link BaseEntity} classes.
 * 
 * @author vrouvine
 */
public class SelectableEntityDataModel<T extends BaseEntity> extends ListDataModel<T> implements SelectableDataModel<T> {

    private T entity;

    public SelectableEntityDataModel() {
    }

    public SelectableEntityDataModel(List<T> entities) {
        super(entities);
    }

    @Override
    public Object getRowKey(T t) {
        return t.getId();
    }

    @Override
    public T getRowData(String rowKey) {
        List<T> wrappedData = getWrappedData();
        for(T e : wrappedData) {
            if(e.getId().equals(Long.parseLong(rowKey))) {
                return e;
            }
        }
        return null;
    }

    @Override
    public List<T> getWrappedData() {
        return (List<T>) super.getWrappedData();
    }
    
    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }
}
