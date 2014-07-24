/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.UserFileEJB;
import com.nokia.ci.ejb.model.FileType;
import com.nokia.ci.ejb.model.OwnershipScope;
import com.nokia.ci.ejb.model.UserFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import javax.faces.bean.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@Named
@ApplicationScoped
public class UserFileStreamBean implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(UserFileStreamBean.class);
    @Inject
    UserFileEJB userFileEJB;
    @Inject
    HttpSessionBean httpSessionBean;

    public StreamedContent getFileStreamParam() {
        StreamedContent ret = new DefaultStreamedContent();
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return ret;
        }
        String id = context.getExternalContext().getRequestParameterMap().get("uuid");

        if (StringUtils.isEmpty(id)) {
            return ret;
        }

        return getFileStream(id);
    }

    public StreamedContent getFileStream(String uuid) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            // http://stackoverflow.com/questions/8304967/how-to-use-pgraphicimage-with-streamedcontent-within-pdatatable
            return new DefaultStreamedContent();
        }

        try {
            if (StringUtils.isEmpty(uuid) || !httpSessionBean.isUploadingEnabled()) {
                return null;
            }

            UserFile userFile = userFileEJB.getUserFileByUuid(uuid);

            boolean streamFile = false;

            // Users own file
            if (userFile.getOwnershipScope() == OwnershipScope.PRIVATE
                    && userFile.getOwner().getId().equals(httpSessionBean.getSysUserId())) {
                streamFile = true;
            }

            // User image or GLOBAL file
            if (userFile.getFileType() == FileType.USER_IMAGE || userFile.getOwnershipScope() == OwnershipScope.GLOBAL) {
                streamFile = true;
            }

            if (streamFile) {
                String path = userFile.getFilePath() + userFile.getUuid();
                File file = new File(path);
                DefaultStreamedContent ret = new DefaultStreamedContent(new FileInputStream(file), userFile.getMimeType(), userFile.getName());
                return ret;
            }
        } catch (Exception ex) {
            log.info("File not found or file does not exist with UUID {}", uuid);
        }

        log.warn("User {} tried to access unauthorized file with UUID: {}", httpSessionBean.getSysUser(), uuid);
        return null;
    }
}
