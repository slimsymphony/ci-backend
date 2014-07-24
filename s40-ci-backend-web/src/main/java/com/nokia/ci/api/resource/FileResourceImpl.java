package com.nokia.ci.api.resource;

import com.nokia.ci.client.rest.FileResource;
import com.nokia.ci.ejb.UserFileEJB;
import com.nokia.ci.ejb.model.AccessScope;
import com.nokia.ci.ejb.model.UserFile;
import java.io.File;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST resource for {@link File} entities.
 *
 * @author larryang
 */
@Named
@RequestScoped
public class FileResourceImpl implements FileResource {

    private static Logger log = LoggerFactory.getLogger(FileResourceImpl.class);
    /**
     * UserFile EJB.
     */
    @Inject
    UserFileEJB userFileEJB;

    @Override
    public Response getRestFile(String uuid) {
        log.debug("Requesting File by uuid {}", uuid);

        try {
            UserFile userFile = userFileEJB.getUserFileByUuid(uuid);

            if (userFile != null && userFile.getAccessScope() != AccessScope.ALL && userFile.getAccessScope() != AccessScope.REST) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            return populateResponse(userFile);

        } catch (Exception ex) {
            log.debug("Exception when request file by uuid. Detailed reasons:{}.", ex);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Override
    public Response getSystemFile(String uuid) {
        log.debug("Requesting System File by uuid {}", uuid);

        try {
            UserFile userFile = userFileEJB.getUserFileByUuid(uuid);

            if (userFile != null && userFile.getAccessScope() != AccessScope.SYSTEM) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            return populateResponse(userFile);

        } catch (Exception ex) {
            log.debug("Exception when request file by uuid. Detailed reasons:{}.", ex);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    private Response populateResponse(UserFile userFile) {
        if (userFile != null) {
            File targetFile = new File(userFile.getFilePath() + userFile.getUuid());
            Response.ResponseBuilder builder = Response.ok(targetFile, userFile.getMimeType());

            builder.header("Content-Disposition", "attachment; filename=\"" + userFile.getName() + "\"");
            builder.header("Content-Length", userFile.getFileSize().toString());

            return builder.build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
