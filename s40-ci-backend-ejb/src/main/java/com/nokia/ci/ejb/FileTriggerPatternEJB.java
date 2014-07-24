package com.nokia.ci.ejb;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.ChangeFile;
import com.nokia.ci.ejb.model.FileTriggerPattern;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author kelvin zhu
 */
@Stateless
@LocalBean
public class FileTriggerPatternEJB extends CrudFunctionality<FileTriggerPattern> implements Serializable {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(FileTriggerPatternEJB.class);
    @EJB
    JobEJB jobEJB;

    public FileTriggerPatternEJB() {
        super(FileTriggerPattern.class);
    }

    public boolean checkFilePathMatch(Long jobId, List<Change> changes) {
        List<FileTriggerPattern> triggerPatterns = jobEJB.getFileTriggerPatterns(jobId);
        boolean matched = false;

        if (!triggerPatterns.isEmpty()) {

            log.debug("Has file trigger patterns, performing check.");

            for (Change change : changes) {
                if (matched) {
                    break;
                }

                List<ChangeFile> changeFiles = change.getChangeFiles();
                for (ChangeFile file : changeFiles) {

                    if (StringUtils.isEmpty(file.getFilePath())) {
                        continue;
                    }

                    if (matched) {
                        break;
                    }

                    for (FileTriggerPattern trigger : triggerPatterns) {
                        if (StringUtils.isEmpty(trigger.getFilepath())) {
                            continue;
                        }

                        String filePath = file.getFilePath();
                        String triggerPath = trigger.getFilepath();

                        if (filePath.startsWith(triggerPath)) {
                            log.debug("File path " + file.getFilePath() + " matched pattern.");
                            matched = true;
                            break;
                        }

                        triggerPath = ".*" + triggerPath + ".*";
                        Pattern pattern = Pattern.compile(triggerPath);
                        Matcher matcher = pattern.matcher(filePath);
                        if (matcher.matches()) {
                            log.debug("File path " + file.getFilePath() + " matched pattern.");
                            matched = true;
                            break;
                        }
                    }
                }
            }
        }

        return matched;
    }
}
