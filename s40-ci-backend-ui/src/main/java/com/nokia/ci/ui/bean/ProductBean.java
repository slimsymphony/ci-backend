package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.ProductEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Product;
import java.util.UUID;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managed bean class for product.
 *
 * @author vrouvine
 */
@Named
@ViewScoped
public class ProductBean extends AbstractUIBaseBean {

    private static Logger log = LoggerFactory.getLogger(ProductBean.class);
    private Product product;
    @Inject
    private ProductEJB productEJB;

    @Override
    protected void init() throws NotFoundException {
        String productId = getQueryParam("productId");
        if (productId == null) {
            product = new Product();
            return;
        }
        log.debug("Finding product {} for editing!", productId);
        product = productEJB.read(Long.parseLong(productId));
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String save() {
        log.debug("Save triggered!");

        if (product.getUuid() == null || product.getUuid().isEmpty()){
            product.setUuid(UUID.randomUUID().toString());
        }
        
        try {
            if (product.getId() != null) {
                log.debug("Updating existing product {}", product);
                productEJB.update(product);
            } else {
                log.debug("Saving new product!");
                productEJB.create(product);
            }
            return "products?faces-redirect=true";
        } catch (NotFoundException nfe) {
            log.warn("Can not save product {}! Cause: {}", product, nfe.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Product could not be saved!", "");
        }

        return null;
    }

    public String cancelEdit() {
        return "products?faces-redirect=true";
    }
}
