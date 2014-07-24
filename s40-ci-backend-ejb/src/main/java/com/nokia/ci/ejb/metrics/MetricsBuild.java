/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.metrics;

import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildStatus;
import java.util.Date;

/**
 * Basic metrics build data for test metrics.
 * @author larryang
 */

public class MetricsBuild extends MetricsVerification{
    
    // Members from BUILD_VERIFICATION_CONF table.
    private String verificationUuid;
    private String productUuid;
    

    public MetricsBuild (Long id, BuildStatus status, Date startTime, Date endTime, String verificationUuid, String productUuid) {
        
        // Members from actual build table.
        super(id, status, startTime, endTime);

        this.verificationUuid = verificationUuid;
        this.productUuid = productUuid;
    }

    /**
     * @return the product UUID
     */
    public String getProductUuid() {
        return productUuid;
    }

    /**
     * @param productUuid the productUuid to set
     */
    public void setProductUuid(String productUuid) {
        this.productUuid = productUuid;
    }

    /**
     * @return the verification UUID
     */
    public String getVerificationUuid() {
        return verificationUuid;
    }

    /**
     * @param verificationUuid the verificationUuid to set
     */
    public void setVerificationUuid(String verificationUuid) {
        this.verificationUuid = verificationUuid;
    }

}
