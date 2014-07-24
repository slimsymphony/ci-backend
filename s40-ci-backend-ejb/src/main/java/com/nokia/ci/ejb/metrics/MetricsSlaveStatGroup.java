package com.nokia.ci.ejb.metrics;

/**
 *
 * @author larryang
 */
public class MetricsSlaveStatGroup extends MetricsGroup<MetricsSlaveStat> {
    
    private Long summarizedTotal = 0L;
    private Long summarizedReserved = 0L;
    private Long averageTotal = null;
    private Long averageReserved = null;


    public void add(MetricsSlaveStat mss) {
        getItems().add(mss);
        
        if (mss.getReservedInstanceCount() != null){
            summarizedReserved += mss.getReservedInstanceCount();
        }
        
        if (mss.getTotalInstanceCount() != null){
            summarizedTotal += mss.getTotalInstanceCount();
        }
    }


    public long getReservedAverage() {
        if (getItems().isEmpty()) {
            return 0L;
        }

        if (averageReserved == null) {
            averageReserved = summarizedReserved / getItems().size();
        }

        return averageReserved;
    }
    
    public long getTotalAverage() {
        if (getItems().isEmpty()) {
            return 0L;
        }

        if (averageTotal == null) {
            averageTotal = summarizedTotal / getItems().size();
        }

        return averageTotal;
    }

    public Long getSummarizedReserved() {
        return summarizedReserved;
    }

    public void setSummarizedReserved(Long summarizedReserved) {
        this.summarizedReserved = summarizedReserved;
    }

    public Long getSummarizedTotal() {
        return summarizedTotal;
    }

    public void setSummarizedTotal(Long summarizedTotal) {
        this.summarizedTotal = summarizedTotal;
    }

}
