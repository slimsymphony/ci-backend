package com.nokia.ci.ejb.tas;

import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.exception.TasProductReadException;
import com.nokia.ci.ejb.model.SysConfigKey;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class TasReaderEJB {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(TasReaderEJB.class);
    private int TIMEOUT = 7000;
    private int connectionTimeout = TIMEOUT;
    private int socketTimeout = TIMEOUT;
    @Inject
    private SysConfigEJB sysConfigEJB;
    
    public List<TasDevice> getAllTasDevices() throws TasProductReadException {
        log.info("starting to fetch tas products");
        long startTime = System.currentTimeMillis();
        
        connectionTimeout = sysConfigEJB.getValue(SysConfigKey.HTTP_CLIENT_CONNECTION_TIMEOUT, TIMEOUT);
        socketTimeout = sysConfigEJB.getValue(SysConfigKey.HTTP_CLIENT_SOCKET_TIMEOUT, TIMEOUT);
        String url = sysConfigEJB.getValue(SysConfigKey.TAS_PRODUCT_SERVER_URL, null);
        if(url == null) {
            throw new TasProductReadException("tas product server not defined");
        }
        
        List<TasDevice> output = null;
        
        try {
            output = TasReader.getTasDevices(url, connectionTimeout, socketTimeout);
        } catch(TasDeviceReadException ex) {
            // wrap to ejb exception.
            throw new TasProductReadException(ex);
        }                
        
        log.info("finished fetch tas products in time(s): {}", (System.currentTimeMillis() - startTime) / 1000);
        
        return output;
    }
}
