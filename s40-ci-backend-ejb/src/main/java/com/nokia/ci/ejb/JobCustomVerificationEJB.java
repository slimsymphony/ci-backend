package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.BranchVerificationConf;
import com.nokia.ci.ejb.model.CustomVerificationConf;
import com.nokia.ci.ejb.model.CustomVerificationParam;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.JobCustomVerification;
import com.nokia.ci.ejb.model.JobCustomVerification_;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.ProjectVerificationConf;
import com.nokia.ci.ejb.model.AbstractVerificationConf;
import com.nokia.ci.ejb.model.TemplateCustomVerification;
import com.nokia.ci.ejb.util.RelationUtil;
import com.nokia.ci.ejb.util.VerificationConfUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business logic implementation for {@link JobCustomVerification} object
 * operations.
 *
 * @author vrouvine
 */
@Stateless
@LocalBean
public class JobCustomVerificationEJB extends CrudFunctionality<JobCustomVerification> implements Serializable {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(JobCustomVerificationEJB.class);

    public JobCustomVerificationEJB() {
        super(JobCustomVerification.class);
    }

    /**
     * Gets custom verification parameters for {@link JobCustomVerification}.
     *
     * @param id Custom verification id
     * @return List of custom verification parameters
     */
    public List<CustomVerificationParam> getCustomVerificationParams(Long id) {
        return getJoinList(id, JobCustomVerification_.customVerificationParams);
    }

    /**
     * Gets custom verification configurations for
     * {@link JobCustomVerification}.
     *
     * @param id Custom verification id
     * @return List of custom verification configurations
     */
    public List<CustomVerificationConf> getCustomVerificationConfs(Long id) {
        return getJoinList(id, JobCustomVerification_.customVerificationConfs);
    }

    /**
     * Return enabled custom verification configurations for
     * {@link JobCustomVerification}. Configuration is enabled only if it is
     * selected in project level.
     *
     * @param id JobCustomVerification id
     * @return List of enabled custom verification configurations
     * @throws NotFoundException If {@link JobCustomVerification} not found with
     * given id
     */
    public List<CustomVerificationConf> getEnabledCustomVerificationConfs(Long id) throws NotFoundException {
        JobCustomVerification jcv = read(id);
        List<CustomVerificationConf> confs = new ArrayList<CustomVerificationConf>();
        Job job = jcv.getJob();
        if (job == null) {
            log.warn("Job is null for {}", jcv);
            return confs;
        }
        Branch branch = job.getBranch();
        if (branch == null) {
            log.warn("Branch is null for {}", job);
            return confs;
        }
        Project project = branch.getProject();
        if (project == null) {
            log.warn("Project is null for {}", branch);
        }
        
        List<ProjectVerificationConf> projectVerificationConfs = project.getVerificationConfs();
        List<BranchVerificationConf> branchVerificationConfs = branch.getVerificationConfs();

        List<? extends AbstractVerificationConf> enabledVerifications = VerificationConfUtil.getEnabledConfs(projectVerificationConfs, branchVerificationConfs);
        for (CustomVerificationConf customConf : jcv.getCustomVerificationConfs()) {
            if (VerificationConfUtil.isCombinationSelected(enabledVerifications,
                    customConf.getProduct(), jcv.getVerification())) {
                confs.add(customConf);
            }
        }
        return confs;
    }
  
    /**
     * Saves given custom verification configurations for the
     * {@link JobCustomVerification}. Whole verification configurations
     * collection is replaced with given collection.
     *
     * @param id Job custom verification id
     * @param confs List of custom verification configurations.
     * @return Updated {@link JobCustomVerification}.
     * @throws NotFoundException If {@link JobCustomVerification} not found with
     * given id.
     */
    public JobCustomVerification saveCustomVerificationConfs(Long id, List<CustomVerificationConf> confs) throws NotFoundException {
        
        JobCustomVerification customVerification = read(id);
        
        List<CustomVerificationConf> toBeDeletedConfs = new ArrayList<CustomVerificationConf>();
        
        for (CustomVerificationConf customVerificationConf : customVerification.getCustomVerificationConfs()) {
            
            if (!customVerificationExists(confs, customVerificationConf)){
                em.remove(customVerificationConf);
                toBeDeletedConfs.add(customVerificationConf);
            }
        }
        
        customVerification.getCustomVerificationConfs().removeAll(toBeDeletedConfs);
        
        log.info("Saving verification configurations for custom verification {}", customVerification);
        
        for (CustomVerificationConf customConf : confs) {
            
            if (!customVerificationExists(customVerification.getCustomVerificationConfs(), customConf)){
                RelationUtil.relate(customVerification, customConf);
            }else{
                customVerificationFindUpdate(customVerification, customConf);
            }
        }
        
        return em.merge(customVerification);
    }
    
    protected void customVerificationFindUpdate(JobCustomVerification customVerification, CustomVerificationConf targetCvc){
        
        for (CustomVerificationConf customVerificationConf : customVerification.getCustomVerificationConfs()){
            if (customVerificationConfsMatch(customVerificationConf, targetCvc) 
                    && customVerificationConf.getCardinality() != targetCvc.getCardinality()){
                customVerificationConf.setCardinality(targetCvc.getCardinality());
            }
        }
    }
    
    protected boolean customVerificationExists(List<CustomVerificationConf> customVerificationConfs, CustomVerificationConf targetJvc){
        
        for (CustomVerificationConf customVerificationConf : customVerificationConfs){
            if (customVerificationConfsMatch(customVerificationConf, targetJvc)){
                return true;
            }
        }
        
        return false;
    }
    
    protected boolean customVerificationConfsMatch(CustomVerificationConf confA, CustomVerificationConf confB){

        try{
            if (confA.getProduct().equals(confB.getProduct())
                    && confA.getCustomVerification().equals(confB.getCustomVerification())){
                return true;
            }else{
                return false;
            }
        }catch(Exception e){
            log.warn("Exception in customVerificationConfsMatch. {}", e.getMessage() + e.getStackTrace());
            return false;
        }
    }
}
