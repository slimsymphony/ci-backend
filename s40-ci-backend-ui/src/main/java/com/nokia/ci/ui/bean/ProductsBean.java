package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.ProductEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Product;
import com.nokia.ci.ejb.model.Project;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managed bean class for products.
 *
 * @author vrouvine
 */
@Named
public class ProductsBean extends DataFilterBean<Product> {

    private static Logger log = LoggerFactory.getLogger(ProductBean.class);
    private List<Product> products;
    @Inject
    private ProductEJB productEJB;

    @Override
    protected void init() {
        initProducts();
    }

    public List<Product> getProducts() {
        return products;
    }

    public void delete(Product product) {
        log.info("Deleting product {}", product);

        List<Project> projects = productEJB.getProjects(product.getId());
        if (!projects.isEmpty()) {
            log.warn("Deleting product {} failed! Cause: product has project(s) that depend on it.", product);
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Delete product failed!", "Selected product could not be deleted! Product has project(s) that depend on it.");
            return;
        }

        try {
            String name = product.getName();
            productEJB.delete(product);
            products.remove(product);
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "Product " + name + " was deleted.");
        } catch (NotFoundException ex) {
            log.warn("Deleting product {} failed! Cause: {}", product, ex.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Delete product failed!", "Selected product could not be deleted!");
        }
    }

    private void initProducts() {
        products = productEJB.readAll();
    }
}
