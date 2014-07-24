package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.JobVerificationConf;
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
public class JobVerificationConfEJB extends CrudFunctionality<JobVerificationConf> implements Serializable {

    private static Logger log = LoggerFactory.getLogger(JobVerificationConfEJB.class);

    @EJB
    SysConfigEJB sysConfigEJB;

    public JobVerificationConfEJB() {
        super(JobVerificationConf.class);
    }
    
    public JobVerificationConf addFile(Long id, UserFile userFile) throws NotFoundException {
        log.debug("Add user file to JobVerificationConf id {}", id);
        long startTime = System.currentTimeMillis();

        JobVerificationConf jobVerificationConf = read(id);

        RelationUtil.relate(jobVerificationConf, userFile);

        log.debug("Finished adding file for JobVerificationConf {}. task done in {}ms", id, System.currentTimeMillis() - startTime);

        return jobVerificationConf;
    }
    
    public List<UserFile> getFiles(Long id) throws NotFoundException {
        JobVerificationConf jobVerificationConf = read(id);
        List<UserFile> userFiles = jobVerificationConf.getUserFiles();
        log.info("Get {} user files for custom verification conf id {}.", userFiles.size(), id);
        return userFiles;
    }
    
    public void updateFiles(Long id, List<UserFile> removeFiles, List<UserFile> addFiles) throws NotFoundException {
        JobVerificationConf jobVerificationConf = read(id);
        jobVerificationConf.getUserFiles().removeAll(removeFiles);
        jobVerificationConf.getUserFiles().addAll(addFiles);
        update(jobVerificationConf);
    }
}