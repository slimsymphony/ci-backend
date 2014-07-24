package com.nokia.ci.ejb.git;

import java.util.concurrent.Future;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author miikka
 */
@LocalBean
@Stateless
@TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
public class AsyncGitFetcher {

    private static Logger log = LoggerFactory.getLogger(AsyncGitFetcher.class);

    @Asynchronous
    public Future<String> fetch(String remoteURI, String localPath, String branch) {
        String fetchHead = "";
        long startTime = System.currentTimeMillis();
        try {
            log.info("Fetching repository {} to {}", remoteURI, localPath);
            GitUtils.fetchRepository(localPath, remoteURI, branch);
            log.info("Fetch success");
            log.info("Obtaining FETCH_HEAD");
            fetchHead = GitUtils.getFetchHead(localPath, branch);
            log.info("FETCH_HEAD for local repository {} was {}", localPath, fetchHead);
        } catch (Throwable t) {
            log.error("Fetch operation FAILED!", t);
        }
        log.info("Fetching done in {}ms", System.currentTimeMillis() - startTime);
        AsyncResult<String> result = new AsyncResult<String>(fetchHead);
        return result;
    }
}
