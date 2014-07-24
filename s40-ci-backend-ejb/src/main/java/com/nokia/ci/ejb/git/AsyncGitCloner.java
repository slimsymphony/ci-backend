package com.nokia.ci.ejb.git;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.concurrent.Future;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author miikka
 */
@LocalBean
@Stateless
@TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
public class AsyncGitCloner {

    private static Logger log = LoggerFactory.getLogger(AsyncGitCloner.class);

    @Asynchronous
    public Future<String> clone(String remoteURI, String localPath, String branch, String sshUserName) {
        String fetchHead = "";
        long startTime = System.currentTimeMillis();
        // Clean local repository if exists.
        cleanLocalRepository(localPath);
        try {
            log.info("Cloning repository {} to {}", remoteURI, localPath);
            GitUtils.cloneRepository(localPath, remoteURI, branch, sshUserName, "");
            log.info("Clone success");
            log.info("Obtaining FETCH_HEAD");
            fetchHead = GitUtils.getFetchHead(localPath, branch);
            log.info("FETCH_HEAD for local repository {} was {}", localPath, fetchHead);
        } catch (Throwable t) {
            log.error("Clone operation FAILED!", t);
        }
        log.info("Cloning done in {}ms", System.currentTimeMillis() - startTime);
        AsyncResult<String> result = new AsyncResult<String>(fetchHead);
        return result;
    }

    private void cleanLocalRepository(String localPath) {
        try {
            // TODO: figure out a better solution than system exec
            log.warn("Deleting local repository {}.", localPath);
            Process p = Runtime.getRuntime().exec("rm -rf " + localPath);
            BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));  
            String line = null;  
            while ((line = err.readLine()) != null) {  
                log.error("Delete process error: " + line);  
            }
            if (p.waitFor() != 0) {
                StringBuilder newPath = new StringBuilder(StringUtils.removeEnd(localPath, "/"));
                newPath.append("_failed_").append(System.currentTimeMillis()).append("/");
                log.error("Deleting local repository failed! Moving local repository to path: {}", newPath.toString());
                Process rename = Runtime.getRuntime().exec("mv " + localPath + " " + newPath.toString());
                BufferedReader err_rename = new BufferedReader(new InputStreamReader(rename.getErrorStream()));  
                String line_rename = null;  
                while ((line_rename = err_rename.readLine()) != null) {  
                    log.error("Move process error: " + line_rename);  
                }
            }
        } catch (IOException ex) {
            log.error("Deleting local repository failed! Repository needs manual cleanup!", ex);
        } catch (InterruptedException e) {
            log.error("Deleting local repository interrupted! Repository needs manual cleanup!", e);
        }
    }
}