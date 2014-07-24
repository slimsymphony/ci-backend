/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.Product;
import com.nokia.ci.ejb.model.Product_;
import com.nokia.ci.ejb.model.Project;
import java.io.Serializable;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 */
@Stateless
@LocalBean
public class ProductEJB extends CrudFunctionality<Product> implements Serializable {
    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ProductEJB.class);

    public ProductEJB() {
        super(Product.class);
    }
    
    public List<Project> getProjects(Long id) {
        return getJoinList(id, Product_.projects);
    }
}
