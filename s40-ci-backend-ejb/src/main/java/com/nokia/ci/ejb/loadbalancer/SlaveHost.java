/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.loadbalancer;

/**
 * @author jaakkpaa This is an auxiliary class to display the accumulation of
 * slaves on host
 */
public class SlaveHost
        implements Comparable<SlaveHost> {

    String hostname;
    int available = 0;
    int reserved;
    Integer total = null;
    Float rank = null;

    public SlaveHost(String hostname) {
        this.hostname = hostname;
    }

    public String getHostname() {
        return (this.hostname);

    }

    public void calculateRank() {
        this.rank = new Float((float) (this.available - 1) / (float) this.total);
    }

    public SlaveHost(String hostname, int available, int total) {
        this.hostname = hostname;
        this.available = available;
        this.reserved = total - available;
        this.total = new Integer(total);
        calculateRank();
    }

    public void setTotal(int total) {
        this.total = new Integer(total);
        this.reserved = total - available;
        calculateRank();
    }

    public void setAvailable(int available) {
        this.available = available;
        if (this.total != null) {
            this.reserved = this.total - available;
            calculateRank();
        }
    }

    // This has lower rank and lower preference = -1
    @Override
    public int compareTo(SlaveHost other) {
        if (this.reserved == 0) // No slaves reserved on this
        {
            if (other.reserved == 0) // No reserved slaves on either 
            {
                // Decide by slave total count
                return (this.total.compareTo(other.total));
            } else // Slaves reserved only on second one 
            {
                // This  is better
                return (1);
            }
        } else // this has reserved slaves
        {
            if (other.reserved == 0) // other has no reserved slaves
            {
                // other is better
                return (-1);
            } else // Both have some reserved slaves
            {
                // Decide by available/total ratio
                int rank = this.rank.compareTo(other.rank);
                if (rank != 0) {
                    return (rank);
                } else {
                    return (this.total.compareTo(other.total));
                }
            }
        }
    }
}
