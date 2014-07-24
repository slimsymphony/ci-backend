/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.metrics;

/**
 *
 * @author jajuutin
 */
public class MetricsHangtimeGroup extends MetricsGroup<MetricsHangtime> {
    private Long summarizedHangtime = 0L;
    private Long hangtimeAverage = 0L;
    
    /**
     * Append a metrics build into group. Use only this method to change
     * contents of underlying list of hangtimes.
     *
     * @param mh
     */
    public void add(MetricsHangtime mh) {
        getItems().add(mh);

        final Long individualHangtime = mh.getHangtime();
        if (individualHangtime != null) {
            summarizedHangtime += individualHangtime;
        }

        hangtimeAverage = null;
    }    

    /**
     * @return the hangtimeAverage
     */
    public Long getHangtimeAverage() {
        if(hangtimeAverage == null) {
            hangtimeAverage = summarizedHangtime / getItems().size();
        }
        return hangtimeAverage;
    }

    /**
     * @param hangtimeAverage the hangtimeAverage to set
     */
    public void setHangtimeAverage(Long hangtimeAverage) {
        this.hangtimeAverage = hangtimeAverage;
    }
}
