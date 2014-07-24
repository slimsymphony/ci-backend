/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.Widget;
import com.nokia.ci.ejb.model.WidgetSetting;
import com.nokia.ci.ejb.model.WidgetSetting_;
import com.nokia.ci.ejb.model.Widget_;
import java.io.Serializable;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@Stateless
@LocalBean
public class WidgetEJB extends CrudFunctionality<Widget> implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(WidgetEJB.class);

    public WidgetEJB() {
        super(Widget.class);
    }

    public List<WidgetSetting> getSettings(Long id) {
        return getJoinList(id, Widget_.settings);
    }
}
