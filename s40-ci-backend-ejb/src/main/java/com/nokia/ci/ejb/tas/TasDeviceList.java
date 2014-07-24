/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.tas;

import java.util.List;

/**
 *
 * @author jajuutin
 */
public class TasDeviceList {
    private List<TasDevice> products;

    public List<TasDevice> getProducts() {
        return products;
    }

    public void setProducts(List<TasDevice> products) {
        this.products = products;
    }
}
