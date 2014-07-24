package com.nokia.ci.ui.model;

import com.nokia.ci.ejb.gerrit.model.GerritDetail;
import java.util.List;
import javax.faces.model.ListDataModel;
import org.primefaces.model.SelectableDataModel;
/**
 * Data model class for Gerrit details.
 * 
 * @author vrouvine
 */
public class GerritDetailDataModel extends ListDataModel<GerritDetail> implements SelectableDataModel<GerritDetail> {

    private GerritDetail detail;

    public GerritDetailDataModel(List<GerritDetail> details) {
        super(details);
    }

    @Override
    public Object getRowKey(GerritDetail t) {
        return t.getId();
    }

    @Override
    public GerritDetail getRowData(String key) {
        List<GerritDetail> wrappedData = (List<GerritDetail>) getWrappedData();
        for (GerritDetail gerritDetail : wrappedData) {
            if (gerritDetail.getId().equals(key)) {
                return gerritDetail;
            }
        }
        return null;
    }

    public GerritDetail getDetail() {
        return detail;
    }

    public void setDetail(GerritDetail detail) {
        this.detail = detail;
    }
}