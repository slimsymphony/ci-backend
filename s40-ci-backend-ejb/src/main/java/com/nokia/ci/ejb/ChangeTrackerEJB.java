package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.ChangeTracker;
import java.io.Serializable;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business logic implementation for {@link ChangeTracker} object operations.
 *
 * @author jajuutin
 */
@Stateless
@LocalBean
public class ChangeTrackerEJB extends CrudFunctionality<ChangeTracker> implements Serializable {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ChangeTrackerEJB.class);

    public ChangeTrackerEJB() {
        super(ChangeTracker.class);
    }
}
