package com.nokia.ci.ui.converter;

import com.nokia.ci.ejb.ProductEJB;
import com.nokia.ci.ejb.model.Product;
import javax.enterprise.context.RequestScoped;
import javax.faces.convert.FacesConverter;

/**
 * Converter for product entities.
 *
 * @author vrouvine
 */
@RequestScoped
@FacesConverter("ProductConverter")
public class ProductConverter extends EntityConverter<Product> {

    public ProductConverter() {
        super(ProductEJB.class);
    }
}
