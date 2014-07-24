package com.nokia.ci.ejb.incident;

import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.event.AuthFailureEvent;
import com.nokia.ci.ejb.event.AuthSuccessEvent;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.IncidentType;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.model.SysUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 3/20/13
 * Time: 10:19 AM
 * To change this template use File | Settings | File Templates.
 */
@Stateless
@LocalBean
public class UnsuccessfulLoginHandlerEJB extends BaseIncidentHandler {

    private static Logger log = LoggerFactory.getLogger(UnsuccessfulLoginHandlerEJB.class);

    private final int UNSUCCESSFUL_LOGIN_THRESHOLD_FOR_USER_SIGNIN_DEFAULT_VALUE = 5;
    private final int UNSUCCESSFUL_LOGIN_MONITOR_PERIOD_DEFAULT_VALUE=2;

    @EJB
    private SysUserEJB sysUserEJB;
    @EJB
    private SysConfigEJB sysConfigEJB;

    private final String USER_SIGNIN_MONITOR_INCIDENT_DESCRIPTION = "User '%s' was attempting to sign in for %s times, without any success.";

    @Asynchronous
    public void loginSuccess(@Observes(during = TransactionPhase.AFTER_SUCCESS) @AuthSuccessEvent Long id) {
        log.info("Handling loginSuccess for user id: {}", id);
        try {
            SysUser sysUser = sysUserEJB.read(id);
            loginSucceed(sysUser);
        } catch (NotFoundException e) {
            log.error("SysUser with user id : {} not found",id);
        }
    }

    @Asynchronous
    public void loginFailure(@Observes(during = TransactionPhase.AFTER_SUCCESS) @AuthFailureEvent String userName) {
        log.info("Handling loginFailure for username: {}", userName);
        try {
            SysUser sysUser = sysUserEJB.getSysUser(userName);
            loginFailure(sysUser);
        } catch (NotFoundException e) {
            log.error("SysUser with userName : {} not found",userName);
        }
    }


    private void loginSucceed(SysUser sysUser){
        log.info("Removing user counter for username {}", sysUser.getLoginName());
        resetUnsuccessfulSignInCounter(sysUser);
    }


    private void loginFailure(SysUser sysUser) throws NotFoundException {
        if (isCounterOld(sysUser)) {
            log.info("User counter ( for username: {}) was old and will be recreate ", sysUser.getLoginName());
            createUnsuccessfulSignInCounter(sysUser);
        } else {
            incrementCounter(sysUser);
            log.info("User counter ( for username: {}) was incremented to {}", sysUser.getLoginName(), sysUser.getFailedLogins());
            if ((sysUser.getFailedLogins()) == getUserCounterValueThreshold()) {
                createUserSignInHasFailedManyTimesIncident(sysUser);
            }
        }
    }

    private void incrementCounter(SysUser userCounter) {
        if (userCounter.getFailedLogins() == null) {
            userCounter.setFailedLogins(0L);
        }
        if (userCounter.getFirstFailedLogin() == null) {
            userCounter.setFirstFailedLogin(new Date());
        }
        userCounter.setFailedLogins(userCounter.getFailedLogins() + 1);
    }


    private boolean isCounterOld(SysUser loginCounter) {
        Date currentTime = new Date();
        if (loginCounter.getFirstFailedLogin()==null){
            return false;
        }
        Date unsuccessfulSignInCounterCreated = loginCounter.getFirstFailedLogin();
        long counterLifeTime = currentTime.getTime() - unsuccessfulSignInCounterCreated.getTime();
        return getCounterDismissTimeInMillisecond() < counterLifeTime;
    }

    private void createUserSignInHasFailedManyTimesIncident(SysUser loginCounter) {
        log.info("New Incident for user (username: {}) sigin",loginCounter.getLoginName());
        String description = String.format(USER_SIGNIN_MONITOR_INCIDENT_DESCRIPTION, loginCounter.getLoginName(), loginCounter.getFailedLogins());
        createIncident(IncidentType.SECURITY,description);
    }

    private int getUserCounterValueThreshold() {
        return sysConfigEJB.getValue(SysConfigKey.UNSUCCESSFUL_LOGIN_THRESHOLD_FOR_USER_SIGNIN, UNSUCCESSFUL_LOGIN_THRESHOLD_FOR_USER_SIGNIN_DEFAULT_VALUE);
    }

    private long getCounterDismissTime() {
        return sysConfigEJB.getValue(SysConfigKey.UNSUCCESSFUL_LOGIN_MONITOR_PERIOD, UNSUCCESSFUL_LOGIN_MONITOR_PERIOD_DEFAULT_VALUE);
    }

    private long getCounterDismissTimeInMillisecond() {
        return getCounterDismissTime() * 60 * 1000;
    }

    private void resetUnsuccessfulSignInCounter(SysUser sysUser) {
        sysUser.setFailedLogins(0L);
        sysUser.setFirstFailedLogin(null);
    }

    private void createUnsuccessfulSignInCounter(SysUser sysUser) {
        sysUser.setFailedLogins(1L);
        sysUser.setFirstFailedLogin(new Date());
    }
}
