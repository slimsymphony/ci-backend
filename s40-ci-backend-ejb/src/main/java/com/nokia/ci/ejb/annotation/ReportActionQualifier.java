package com.nokia.ci.ejb.annotation;

import com.nokia.ci.ejb.model.ReportAction;
import javax.enterprise.util.AnnotationLiteral;

/**
 *
 * @author vrouvine
 */
public class ReportActionQualifier extends AnnotationLiteral<ReportActionExecutorType> implements ReportActionExecutorType {
    
    private Class<? extends ReportAction> type;
    
    public ReportActionQualifier(Class<? extends ReportAction> type) {
        this.type = type;
    }
    
    @Override
    public Class<? extends ReportAction> type() {
        return type;
    }

}
