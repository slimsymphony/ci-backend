/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.model.search;

import com.nokia.ci.ejb.model.BaseEntity;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ui.bean.annotation.SearchResult;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author hhellgre
 */
@SearchResult(type = SysUser.class, prefix = "user")
public class SysUserSearchResult extends BaseSearchResult {

    private static final String urlBase = "/secure/pages/userDetails.xhtml?userLogin=";

    public SysUserSearchResult(BaseEntity entity) {
        super(entity);
        SysUser user = (SysUser) entity;
        url = urlBase + user.getLoginName();
        header = "User ";
        if (StringUtils.isNotEmpty(user.getRealName())) {
            header += user.getRealName();
        } else {
            header += user.getLoginName();
        }

        description = user.getLoginName() + " <" + user.getEmail() + ">";
    }
}
