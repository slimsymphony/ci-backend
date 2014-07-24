/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.CIServer;
import java.io.Serializable;
import java.util.UUID;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.LockModeType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@Stateless
@LocalBean
public class CIServerLoadEJB extends CrudFunctionality<CIServer> implements Serializable {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(CIServerLoadEJB.class);

    public CIServerLoadEJB() {
        super(CIServer.class);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void startBuild(Long id) throws NotFoundException {
        CIServer ciServer = readWithLock(id, LockModeType.PESSIMISTIC_WRITE);
        if (StringUtils.isEmpty(ciServer.getUuid())) {
            log.info("CIServer {} does not have UUID, creating new one", ciServer);
            ciServer.setUuid(UUID.randomUUID().toString());
            return;
        }

        int newLoad = ciServer.getBuildsRunning() + 1;
        log.info("Starting buildGroup on CIServer {}, current load is " + newLoad, ciServer);
        ciServer.setBuildsRunning(newLoad);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void finalizeBuild(Long id) throws NotFoundException {
        CIServer ciServer = readWithLock(id, LockModeType.PESSIMISTIC_WRITE);
        if (StringUtils.isEmpty(ciServer.getUuid())) {
            log.info("CIServer {} does not have UUID, creating new one", ciServer);
            ciServer.setUuid(UUID.randomUUID().toString());
            return;
        }
        int newLoad = ((ciServer.getBuildsRunning() - 1) <= 0 ? 0 : (ciServer.getBuildsRunning() - 1));
        log.info("Finalizing buildGroup on CIServer {}, current load is " + newLoad, ciServer);
        ciServer.setBuildsRunning(newLoad);
    }
}
