package com.nokia.ci.ejb;

import com.nokia.ci.ejb.annotation.ReportActionQualifier;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.ReportAction;
import java.io.Serializable;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business logic implementation for {@link ReportAction} object operations.
 *
 * @author vrouvine
 */
@Stateless
@LocalBean
public class ReportActionEJB extends CrudFunctionality<ReportAction> implements Serializable {

    @Inject
    @Any
    private Instance<ReportActionExecutor> reportActionExecutor;
    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ReportActionEJB.class);

    public ReportActionEJB() {
        super(ReportAction.class);
    }

    public void execute(ReportAction action, BuildGroup buildGroup) {
        ReportActionExecutor executor = findExecutor(action);
        executor.execute(action, buildGroup);
    }

    public void execute(ReportAction action, Build build) {
        ReportActionExecutor executor = findExecutor(action);
        executor.execute(action, build);
    }
    
    public void execute(ReportAction action, BuildGroup buildGroup, List<Change> changes) {
        ReportActionExecutor executor = findExecutor(action);
        executor.execute(action, buildGroup, changes);
    }

    private ReportActionExecutor findExecutor(ReportAction action) {
        Class<? extends ReportAction> actionClass = action.getClass();
        log.debug("Finding executor for {}", actionClass);
        return reportActionExecutor.select(new ReportActionQualifier(actionClass)).get();
    }
}
