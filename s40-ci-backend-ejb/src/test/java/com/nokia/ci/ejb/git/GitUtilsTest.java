package com.nokia.ci.ejb.git;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepository;
import org.junit.Test;

public class GitUtilsTest {

    final static String localRepoPath = "/tmp/repo/";
    final static String remoteRepoURI = "ssh://user@gerrit02.nokia.com:29418/ci/ci20test";
    final static String branch = "develop";

    public void tearDown() throws IOException {
        File toBeRemoved = new File(localRepoPath);
        if (toBeRemoved.exists()) {
            FileUtils.deleteDirectory(toBeRemoved);
        }
    }

    public void cloneRepo() throws Exception {
        GitUtils.cloneRepository(localRepoPath, remoteRepoURI, branch, "username", "password");
    }

    public void pushTest() throws Exception {
        File file = new File(localRepoPath + "aa");
        if (!file.exists()) {
            new File(localRepoPath).mkdirs();
            file.createNewFile();
        }
        FileWriter fstream = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fstream);

        out.append("here be content");
        out.close();

        Repository repo = new FileRepository(localRepoPath);
        if (!repo.isBare()) {
            CommitCommand commit = new Git(repo).commit();
            commit.setAuthor("username", "email");
            commit.setMessage("change");
            commit.setAll(true);
            commit.call();
        }
    }

    public void fetchChangesINValid() throws Exception {
        GitUtils.fetchRepository("/tmp/does_not_exists", "http://example.com/repo", "master");
    }

    public void fetchChangesValid() throws Exception {
        GitUtils.fetchRepository(localRepoPath, remoteRepoURI, "master");
    }

    public void getFetchHead() throws GitAPIException, IOException {
        System.out.println(GitUtils.getFetchHead(localRepoPath, branch));
    }
}
