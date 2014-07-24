package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.SysUserImageEJB;
import com.nokia.ci.ejb.WidgetEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ejb.model.Widget;
import com.nokia.ci.ejb.UserFileEJB;
import com.nokia.ci.ejb.model.AccessScope;
import com.nokia.ci.ejb.model.FileType;
import com.nokia.ci.ejb.model.OwnershipScope;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.model.SysUserImage;
import com.nokia.ci.ejb.model.UserFile;
import com.nokia.ci.ejb.model.WidgetSetting;
import com.nokia.ci.ejb.util.RelationUtil;
import com.nokia.ci.ejb.util.TimezoneUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@Named
@ViewScoped
public class UserSettingsBean extends AbstractUIBaseBean {

    private static final Logger log = LoggerFactory.getLogger(UserSettingsBean.class);
    private SysUser user;
    private String selectedTimezone = "Europe/Helsinki";
    private Boolean receiveEmail;
    private Map<String, String> themes;
    private String theme;
    private List<Widget> myWidgets;
    private List<Widget> selectedWidgets;
    private List<UserFile> myFiles;
    private List<UserFile> myFilesFiltered;
    private List<UserFile> globalFiles;
    private List<UserFile> globalFilesFiltered;
    private UserFile userImage;
    @Inject
    HttpSessionBean httpSessionBean;
    @Inject
    SysUserEJB userEJB;
    @Inject
    SysUserImageEJB sysUserImageEJB;
    @Inject
    WidgetEJB widgetEJB;
    @Inject
    UserFileEJB userFileEJB;
    @Inject
    SysConfigEJB sysConfigEJB;

    @Override
    protected void init() throws NotFoundException {
        user = userEJB.read(httpSessionBean.getSysUserId());
        selectedTimezone = user.getTimezone();
        receiveEmail = user.getSendEmail();
        if (receiveEmail == null) {
            receiveEmail = Boolean.TRUE;
        }

        theme = user.getTheme();
        if (theme == null) {
            theme = "ci20theme";
        }

        initThemes();
        initUserWidgets();
        initFiles();
    }

    private void initThemes() {
        themes = new TreeMap<String, String>();
        themes.put("CI 2.0 Theme", "ci20theme");
        themes.put("Ambience", "ambience");
        themes.put("Black tie", "black-tie");
        themes.put("Pipboy 3000", "pipboy-3000");
    }

    private void initUserWidgets() {
        List<Widget> userWidgets = userEJB.getWidgetsInOrder(user.getId());
        selectedWidgets = new ArrayList<Widget>();
        myWidgets = new ArrayList<Widget>();

        myWidgets.addAll(userWidgets);
        selectedWidgets.addAll(userWidgets);
        addUserWidget(userWidgets, "com.nokia.ci.ui.widget.UserJobsWidget", "My toolbox jobs", "myToolboxJobs");
        addUserWidget(userWidgets, "com.nokia.ci.ui.widget.UserBuildsWidget", "My last builds", "myLastBuilds");
        addUserWidget(userWidgets, "com.nokia.ci.ui.widget.UserChangesWidget", "My last changes", "myLastChanges");
    }

    private void initFiles() {
        globalFiles = userFileEJB.getGlobalUserFiles();
        myFiles = userEJB.getFiles(httpSessionBean.getSysUser().getId());
    }

    private void addUserWidget(List<Widget> userWidgets, String className, String header, String identifier) {
        for (Widget w : userWidgets) {
            if (w.getIdentifier().equals(identifier)) {
                return;
            }
        }

        WidgetSetting idSetting = new WidgetSetting();
        idSetting.setSettingKey("id");
        idSetting.setSettingValue(user.getId().toString());

        Widget widget = new Widget();
        widget.setClassName(className);
        widget.setHeader(header);
        widget.setIdentifier(identifier);
        RelationUtil.relate(widget, idSetting);
        myWidgets.add(widget);
    }

    public List<Widget> getMyWidgets() {
        return myWidgets;
    }

    public void setMyWidgets(List<Widget> myWidgets) {
        this.myWidgets = myWidgets;
    }

    public List<Widget> getSelectedWidgets() {
        return selectedWidgets;
    }

    public void setSelectedWidgets(List<Widget> selectedWidgets) {
        this.selectedWidgets = selectedWidgets;
    }

    public SysUser getUser() {
        return user;
    }

    public List<String> getTimezones() {
        return TimezoneUtil.getTimezoneList();
    }

    public String getSelectedTimezone() {
        return selectedTimezone;
    }

    public void setSelectedTimezone(String selectedTimezone) {
        this.selectedTimezone = selectedTimezone;
    }

    public String save() {
        log.debug("Save triggered!");

        try {
            user.setTimezone(selectedTimezone);
            user.setSendEmail(receiveEmail);
            user.setTheme(theme);
            saveWidgets();
            userEJB.update(user);
            httpSessionBean.setSysUser(user);
        } catch (NotFoundException ex) {
            log.warn("Could not save user settings for user {}", user);
        }

        String page = "myToolbox";
        if (user.getDefaultPage() != null && !user.getDefaultPage().isEmpty()) {
            page = user.getDefaultPage();
        }

        return page + "?faces-redirect=true";
    }

    public void saveWidgets() throws NotFoundException {
        List<Widget> removedWidgets = new ArrayList<Widget>();
        removedWidgets.addAll(myWidgets);
        removedWidgets.removeAll(selectedWidgets);
        for (Widget w : removedWidgets) {
            if (w.getId() != null) {
                widgetEJB.delete(w);
            }
        }

        userEJB.saveWidgets(user.getId(), selectedWidgets);
    }

    public String cancelEdit() {
        return "myToolbox?faces-redirect=true";
    }

    public Boolean getReceiveEmail() {
        return receiveEmail;
    }

    public void setReceiveEmail(Boolean receiveEmail) {
        this.receiveEmail = receiveEmail;
    }

    public Map<String, String> getThemes() {
        return themes;
    }

    public void setThemes(Map<String, String> themes) {
        this.themes = themes;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public List<UserFile> getGlobalFiles() {
        return globalFiles;
    }

    public List<UserFile> getMyFiles() {
        return myFiles;
    }

    public List<UserFile> getMyFilesFiltered() {
        return myFilesFiltered;
    }

    public void setMyFilesFiltered(List<UserFile> myFilesFiltered) {
        this.myFilesFiltered = myFilesFiltered;
    }

    public List<UserFile> getGlobalFilesFiltered() {
        return globalFilesFiltered;
    }

    public void setGlobalFilesFiltered(List<UserFile> globalFilesFiltered) {
        this.globalFilesFiltered = globalFilesFiltered;
    }

    public void handleImageUpload(FileUploadEvent event) {
        log.info("Uploaded image" + event.getFile());
        UploadedFile uploadedFile = event.getFile();
        FacesMessage msg;
        if (!sysConfigEJB.configExists(SysConfigKey.USER_FILE_UPLOAD_PATH)) {
            log.warn("USER_FILE_UPLOAD_PATH not configured in system setting.");
            msg = new FacesMessage("Warning! ", uploadedFile.getFileName() + " not uploaded. USER_FILE_UPLOAD_PATH need to be configured.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        try {
            String fileUuid = UUID.randomUUID().toString();
            boolean copied = userFileEJB.copyFile(fileUuid, uploadedFile.getInputstream());

            if (copied == true) {
                if (user.getUserImage() != null && user.getUserImage().getFileUuid() != null) {
                    deleteUserImage();
                }

                userImage = new UserFile();
                userImage.setMimeType(uploadedFile.getContentType());
                userImage.setFileSize(uploadedFile.getSize());
                userImage.setName(uploadedFile.getFileName());
                userImage.setUuid(fileUuid);
                userImage.setFilePath(userFileEJB.getUploadPath());
                userImage.setOwnershipScope(OwnershipScope.PRIVATE);
                userImage.setAccessScope(AccessScope.SYSTEM);
                userImage.setFileType(FileType.USER_IMAGE);
                userEJB.addFile(user.getId(), userImage);

                SysUserImage img = new SysUserImage();
                if (user.getUserImage() != null) {
                    img = user.getUserImage();
                }
                img.setFileUuid(userImage.getUuid());
                img.setSysUser(user);

                if (user.getUserImage() != null) {
                    img = sysUserImageEJB.update(img);
                } else {
                    sysUserImageEJB.create(img);
                }

                httpSessionBean.getSysUser().setUserImage(img);
                user.setUserImage(img);
            } else {
                msg = new FacesMessage("Failure! ", uploadedFile.getFileName() + " can not be uploaded.");
                FacesContext.getCurrentInstance().addMessage(null, msg);
            }
        } catch (Exception e) {
            msg = new FacesMessage("Failure! ", uploadedFile.getFileName() + " can not be uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public void deleteUserFile(UserFile userFile) {
        log.info("Deleting userFile id: {}", userFile.getId());

        try {
            if (!httpSessionBean.getSysUser().equals(userFile.getOwner())) {
                addMessage(FacesMessage.SEVERITY_WARN,
                        "Delete user file failed!", "You are neither the file owner, nor the system admin.");
                log.warn("Deleting user file failed, not the owner. Current session user id is {}, but file owner user id is {}.",
                        httpSessionBean.getSysUser().getId().longValue(), userFile.getOwner().getId().longValue());
                return;
            }

            if (userFile.getFileType() == FileType.USER_IMAGE) {
                deleteUserImage();
                return;
            }

            if (userFile.getFileType() == FileType.TEST_FILE) {
                userFileEJB.deleteTestFileById(userFile.getId());

                myFiles = userEJB.getFiles(httpSessionBean.getSysUser().getId());
                globalFiles = userFileEJB.getGlobalUserFiles();
                return;
            }

            userFileEJB.deleteFileById(userFile.getId());


        } catch (Exception ex) {
            log.warn("Exception when deleting file. Details: {}", ex.getMessage() + ex.getStackTrace());
        }
    }

    private void deleteUserImage() {
        try {
            Long userImageId = httpSessionBean.getSysUser().getUserImage().getId();
            SysUserImage img = sysUserImageEJB.deleteUserImageFile(userImageId);

            user.setUserImage(img);
            httpSessionBean.getSysUser().setUserImage(img);
        } catch (Exception ex) {
        }
    }
}
