package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.CustomVerificationConf;
import com.nokia.ci.ejb.model.FileType;
import com.nokia.ci.ejb.model.JobVerificationConf;
import com.nokia.ci.ejb.model.OwnershipScope;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ejb.model.UserFile;
import com.nokia.ci.ejb.model.UserFile_;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author larryang
 */
@Stateless
@LocalBean
public class UserFileEJB extends SecurityFunctionality<UserFile> implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(UserFileEJB.class);
    @Inject
    private JobVerificationConfEJB jobVerificationConfEJB;
    @Inject
    private CustomVerificationConfEJB customVerificationConfEJB;
    @Inject
    private SysUserEJB sysUserEJB;
    @Inject
    private SysConfigEJB sysConfigEJB;

    public UserFileEJB() {
        super(UserFile.class);
    }

    public UserFile getUserFileByUuid(String uuid) throws NotFoundException {

        log.debug("UserFileEJB.getUserFileByUuid({})", uuid);
        UserFile userFile = null;
        CriteriaQuery<UserFile> query = cb.createQuery(UserFile.class);
        Root<UserFile> root = query.from(UserFile.class);
        query.where(cb.equal(root.get(UserFile_.uuid), uuid));

        // TODO: checkAuth

        try {
            userFile = em.createQuery(query).getSingleResult();
            log.debug("UserFileEJB.getUserFileByUuid return " + userFile);
        } catch (NoResultException nre) {
            log.error("Can't find UserFile for uuid {}", uuid);
            throw new NotFoundException("UserFile not found with UUID: " + uuid);
        }

        return userFile;
    }

    public List<UserFile> getGlobalUserFiles() {

        log.debug("UserFileEJB.getGlobalUserFiles()");
        CriteriaQuery<UserFile> query = cb.createQuery(UserFile.class);
        Root<UserFile> root = query.from(UserFile.class);
        query.where(cb.equal(root.get(UserFile_.ownershipScope), OwnershipScope.GLOBAL));

        // TODO: checkAuth as user cannot access to files that are not his/her own and
        //       do belong to some other project (test files only)

        List<UserFile> userFiles = em.createQuery(query).getResultList();
        log.debug("UserFileEJB.getGlobalUserFiles return {} files." + userFiles.size());
        return userFiles;
    }

    public List<UserFile> getAvailableFilesByUser(Long userId) {

        log.debug("UserFileEJB.getAvailableFilesByUser({})", userId);
        List<UserFile> userFiles = new ArrayList<UserFile>();

        try {
            SysUser owner = sysUserEJB.read(userId);
            CriteriaQuery<UserFile> query = cb.createQuery(UserFile.class);
            Root<UserFile> root = query.from(UserFile.class);
            query.where(cb.or(cb.equal(root.get(UserFile_.owner), owner), cb.equal(root.get(UserFile_.ownershipScope), OwnershipScope.GLOBAL)));
            userFiles = em.createQuery(query).getResultList();
        } catch (Exception e) {
            log.error("Exception when getting available user files by user id " + userId + ", Details {}", e);
        }
        log.debug("UserFileEJB.getAvailableFilesByUser return {} files." + userFiles.size());
        return userFiles;
    }

    public List<UserFile> getAvailableFilesByUserAndType(Long userId, FileType type) {
        log.debug("UserFileEJB.getAvailableFilesByUserAndType({}, {})", userId, type);
        List<UserFile> userFiles = new ArrayList<UserFile>();

        try {
            SysUser owner = sysUserEJB.read(userId);
            CriteriaQuery<UserFile> query = cb.createQuery(UserFile.class);
            Root<UserFile> root = query.from(UserFile.class);
            Predicate userFilePred = cb.or(cb.equal(root.get(UserFile_.owner), owner), cb.equal(root.get(UserFile_.ownershipScope), OwnershipScope.GLOBAL));
            Predicate typePred = cb.equal(root.get(UserFile_.fileType), type);
            query.where(cb.and(userFilePred, typePred));
            userFiles = em.createQuery(query).getResultList();
        } catch (Exception e) {
            log.error("Exception when getting available user files by user id " + userId + " and type " + type + " , Details {}", e);
        }
        log.debug("UserFileEJB.getAvailableFilesByUserAndType return {} files." + userFiles.size());
        return userFiles;
    }

    public void deleteFileById(Long userFileId) throws NotFoundException {
        UserFile userFile = read(userFileId);
        delete(userFile);
    }

    public void deleteTestFileById(Long userFileId) throws NotFoundException {
        log.info("Deleting test file with id: {}", userFileId);
        UserFile userFile = read(userFileId);

        if (userFile.getFileType() != FileType.TEST_FILE) {
            log.warn("UserFile {} is not TEST_FILE", userFile);
            return;
        }

        List<JobVerificationConf> jobVerificationConfs = userFile.getJobVerificationConfs();
        List<CustomVerificationConf> customVerificationConfs = userFile.getCustomVerificationConfs();

        userFile.setJobVerificationConfs(null);
        userFile.setCustomVerificationConfs(null);

        for (JobVerificationConf jobVerificationConf : jobVerificationConfs) {
            jobVerificationConf.getUserFiles().remove(userFile);
            jobVerificationConfEJB.update(jobVerificationConf);
        }

        for (CustomVerificationConf customVerificationConf : customVerificationConfs) {
            customVerificationConf.getUserFiles().remove(userFile);
            customVerificationConfEJB.update(customVerificationConf);
        }

        delete(userFile);
        log.info("Test file {} deleted!", userFile);
    }

    @Override
    public void delete(UserFile userFile) throws NotFoundException {
        deleteFileFromFilesystem(userFile.getId());
        super.delete(userFile);
    }

    public String getUploadPath() {
        String destination = sysConfigEJB.getValue(SysConfigKey.USER_FILE_UPLOAD_PATH, "");
        if (!destination.endsWith("/") && destination.contains("/")) {
            destination += "/";
        } else if (!destination.endsWith("\\") && destination.contains("\\")) {
            destination += "\\";
        }
        return destination;
    }

    private void deleteFileFromFilesystem(Long userFileId) throws NotFoundException {
        UserFile userFile = read(userFileId);

        log.info("Deleting file {} from filesystem", userFile);
        File toBeDeletedFile = new File(userFile.getFilePath() + userFile.getUuid());
        if (toBeDeletedFile.isFile() && toBeDeletedFile.exists()) {
            toBeDeletedFile.delete();
            log.info("File {} deleted", userFile);
            return;
        }
        log.warn("Could not delete userFile {} from filesystem", userFile);
    }

    public boolean copyFile(String fileName, InputStream in) {
        boolean successFlag = true;
        String destination = getUploadPath();
        try {
            // Check for destination directory existing
            File folder = new File(destination);
            if (!folder.exists()) {
                log.info("Upload folder {} does not exist, creating", destination);
                boolean success = folder.mkdirs();
                if (!success) {
                    log.error("Could not create upload directory " + destination + "! Please check access rights!");
                    return false;
                }
            }

            if (!folder.canWrite()) {
                log.error("Cannot write to upload directory " + destination + "!");
                return false;
            }

            // write the inputStream to a FileOutputStream
            OutputStream out = new FileOutputStream(new File(destination + fileName));

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }

            in.close();
            out.flush();
            out.close();
        } catch (IOException e) {
            log.error("Exception when copying uploaded file {}. Detailed reasons: {}.",
                    destination + fileName, e.getMessage() + e.getStackTrace());
            successFlag = false;
        }

        return successFlag;
    }

    public File getFileByUuid(String uuid) throws NotFoundException {
        UserFile file = getUserFileByUuid(uuid);
        return getFile(file);
    }

    public File getFile(UserFile userFile) {
        log.debug("Requesting File {}", userFile);

        if (userFile == null) {
            return null;
        }

        String filePath = userFile.getFilePath();

        if (filePath == null) {
            return null;
        }

        return new File(filePath);
    }
}
