package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.CustomVerificationConf;
import com.nokia.ci.ejb.model.UserFile;
import com.nokia.ci.ejb.util.RelationUtil;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author larryang
 */
@Stateless
@LocalBean
public class CustomVerificationConfEJB extends CrudFunctionality<CustomVerificationConf> implements Serializable {

    private static Logger log = LoggerFactory.getLogger(CustomVerificationConfEJB.class);

    @EJB
    SysConfigEJB sysConfigEJB;

    public CustomVerificationConfEJB() {
        super(CustomVerificationConf.class);
    }
    
    public CustomVerificationConf addFile(Long id, UserFile userFile) throws NotFoundException {
        log.debug("Received {} user file for CustomVerificationConf {}", id);
        long startTime = System.currentTimeMillis();

        CustomVerificationConf customVerificationConf = read(id);

        RelationUtil.relate(customVerificationConf, userFile);

        log.debug("Finished adding file for CustomVerificationConf {}. task done in {}ms", id, System.currentTimeMillis() - startTime);

        return customVerificationConf;
    }

    public List<UserFile> getFiles(Long id) throws NotFoundException {
        CustomVerificationConf customVerificationConf = read(id);
        List<UserFile> userFiles = customVerificationConf.getUserFiles();
        log.info("Got {} user files for custom verification conf id {}.", userFiles.size(), id);
        return userFiles;
    }
    
    public void updateFiles(Long id, List<UserFile> removeFiles, List<UserFile> addFiles) throws NotFoundException {
        CustomVerificationConf customVerificationConf = read(id);
        customVerificationConf.getUserFiles().removeAll(removeFiles);
        customVerificationConf.getUserFiles().addAll(addFiles);
        update(customVerificationConf);
    }
}
