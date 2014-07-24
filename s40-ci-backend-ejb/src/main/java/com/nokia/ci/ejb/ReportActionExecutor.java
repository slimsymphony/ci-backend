package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.ReportAction;
import java.util.List;

/**
 *
 * @author vrouvine
 */
public interface ReportActionExecutor<T extends ReportAction> {
    
    public void execute(T action, BuildGroup buildGroup);
    
    public void execute(T action, Build build);
    
    public void execute(T action, BuildGroup buildGroup, List<Change> changes);
}
