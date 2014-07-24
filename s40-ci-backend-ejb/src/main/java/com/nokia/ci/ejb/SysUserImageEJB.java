/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.FileType;
import com.nokia.ci.ejb.model.SysUserImage;
import com.nokia.ci.ejb.model.UserFile;
import java.io.Serializable;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@Stateless
@LocalBean
public class SysUserImageEJB extends CrudFunctionality<SysUserImage> implements Serializable {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SysUserImageEJB.class);
    @Inject
    private UserFileEJB userFileEJB;

    public SysUserImageEJB() {
        super(SysUserImage.class);
    }

    public SysUserImage deleteUserImageFile(Long id) throws NotFoundException {
        log.info("Deleting user image file with id {}", id);
        SysUserImage img = read(id);
        String fileUuid = img.getFileUuid();

        UserFile userFile = userFileEJB.getUserFileByUuid(fileUuid);
        if (userFile.getFileType() != FileType.USER_IMAGE) {
            log.warn("UserFile {} is not USER_IMAGE!", userFile);
            return img;
        }

        userFileEJB.deleteFileById(userFile.getId());

        log.info("User image file deleted!");
        img.setFileUuid(null);
        return update(img);
    }
}
