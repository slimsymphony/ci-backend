package com.nokia.ci.ui.converter;

import com.nokia.ci.ejb.CrudFunctionality;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.BaseEntity;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract super class for entity converters.
 *
 * @author vrouvine
 */
public abstract class EntityConverter<T extends BaseEntity> implements Converter {

    private static final Logger log = LoggerFactory.getLogger(EntityConverter.class);
    private static final String EJB_JNDI_BASE = "java:global/s40-ci-backend-ear/s40-ci-backend-ejb/";
    private CrudFunctionality ejb;
    private Class ejbClass;

    /**
     * Protected empty constructor.
     */
    protected EntityConverter() {
    }

    /**
     * Creates new instance.
     *
     * @param ejbClass Class of EJB bean.
     */
    public EntityConverter(Class<? extends CrudFunctionality> ejbClass) {
        this.ejbClass = ejbClass;
        init();
    }

    /**
     * Converts string value to type <T> entity. Null and empty string value is
     * converted to {@code null}.
     *
     * @param context FacesContext
     * @param component UIComponent
     * @param value Value to converter
     * @return converted entity.
     */
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        log.debug("Converting value {} to entity", value);
        if (StringUtils.isEmpty(value)) {
            return null;
        }

        T entity = null;
        try {
            entity = (T) ejb.read(Long.parseLong(value));
        } catch (NotFoundException ex) {
            log.warn("Entity not found with value {}! Cause: {}", value, ex.getMessage());
        }
        return entity;
    }

    /**
     * Converts type <T> entity to string using id. Null values are converted to
     * empty string and string values are returned as is.
     *
     * @param context FacesContext
     * @param component UIComponent
     * @param value Entity object to convert
     * @return String value of entity id.
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        log.debug("Converting Object {} to String", value);
        if (value == null) {
            return "";
        }

        if (value instanceof String) {
            return (String) value;
        }

        if (!(value instanceof BaseEntity)) {
            log.error("Object is not subclass of {}", BaseEntity.class);
            throw new ConverterException(createConversionMessage("Not a entity!"));
        }
        BaseEntity entity = (BaseEntity) value;
        return String.valueOf(entity.getId());
    }

    private void init() {
        String ejbJndiName = EJB_JNDI_BASE + ejbClass.getSimpleName();
        try {
            InitialContext ic = new InitialContext();
            ejb = (CrudFunctionality) ic.lookup(ejbJndiName);
        } catch (NamingException ex) {
            log.error("Can not find EJB with name " + ejbJndiName + " from jndi namespace!", ex);
        }
    }

    private FacesMessage createConversionMessage(String detail) {
        FacesMessage message = new FacesMessage("Entity conversion failed!", detail);
        return message;
    }
}
