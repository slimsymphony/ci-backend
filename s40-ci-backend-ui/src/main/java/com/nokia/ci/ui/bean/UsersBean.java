package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ui.model.UsersLazyDataModel;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for users.
 *
 * @author vrouvine
 */
@Named
@RequestScoped
public class UsersBean extends AbstractUIBaseBean {

    private static final Logger log = LoggerFactory.getLogger(UsersBean.class);
    private LazyDataModel<SysUser> users;
    @Inject
    SysUserEJB userEJB;

    @Override
    protected void init() {
        initUsers();
    }

    public LazyDataModel<SysUser> getUsers() {
        return users;
    }

    private void initUsers() {
        users = new UsersLazyDataModel(userEJB);
    }
}
