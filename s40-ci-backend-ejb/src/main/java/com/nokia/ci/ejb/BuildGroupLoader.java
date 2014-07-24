/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.util.Order;
import java.util.List;

/**
 *
 * @author hhellgre
 */
public interface BuildGroupLoader {

    public List<BuildGroup> getBuildGroups(Long id, int first, int pageSize, String orderField, Order order);

    public Long getBuildGroupCount(Long id);
}
