package com.nokia.ci.ui.converter;

import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildGroup;
import java.util.Date;
import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 * Build duration converter. This converter is used to format build duration for
 * output components only.
 *
 * @author vrouvine
 */
@RequestScoped
@FacesConverter("BuildDurationConverter")
public class BuildDurationConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        throw new UnsupportedOperationException("Converting to object is not supported. Do not use this converter as input converter!");
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        Date start = null;
        Date end = null;
        if (value instanceof BuildGroup) {
            BuildGroup buildGroup = (BuildGroup) value;
            start = buildGroup.getStartTime();
            end = buildGroup.getEndTime();
        } else if (value instanceof Build) {
            Build build = (Build) value;
            start = build.getStartTime();
            end = build.getEndTime();
        }

        if (start == null) {
            return "";
        }

        long endTime = System.currentTimeMillis();
        if (end != null) {
            endTime = end.getTime();
        }

        String pattern = (String) component.getAttributes().get("pattern");
        long duration = endTime - start.getTime();
        if (StringUtils.isEmpty(pattern)) {
            return DurationFormatUtils.formatDurationWords(duration, true, false);
        }
        return DurationFormatUtils.formatDuration(duration, pattern);
    }
}
