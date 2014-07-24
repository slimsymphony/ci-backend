package com.nokia.ci.ejb.git;

import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.ChangeFile;
import com.nokia.ci.ejb.model.ChangeFileType;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author miikka
 */
public class GitUtils {

    private static Logger log = LoggerFactory.getLogger(GitUtils.class);

    private static void initRepository(String localPath) throws GitAPIException {
        File workDir = createFolder(localPath);
        InitCommand init = Git.init();
        init.setDirectory(workDir);
        Git git = init.call();
    }

    private static File createFolder(String path) throws IllegalArgumentException {
        File workDir = new File(path);
        workDir.mkdirs();

        if (!workDir.canWrite()) {
            throw new IllegalArgumentException("Cannot use directory: " + path);
        }
        return workDir;
    }

    public static void cloneRepository(String localRepositoryPath, String remoteRepositoryURI, String branch, String username, String password) throws IOException, InvalidRemoteException, GitAPIException {
        File workDir = createFolder(localRepositoryPath);
        CloneCommand clone = Git.cloneRepository();
        clone.setBare(false);
        clone.setCloneAllBranches(false);
        clone.setBranch(branch);
        clone.setDirectory(workDir).setURI(remoteRepositoryURI);
        UsernamePasswordCredentialsProvider credentials = new UsernamePasswordCredentialsProvider(username, password);
        clone.setCredentialsProvider(credentials);
        log.info("Cloning from {} to {} with branch {}...", new Object[]{remoteRepositoryURI, localRepositoryPath, branch});
        long startTime = System.currentTimeMillis();
        Git git = clone.call();
        log.info("Clone done in {}ms", System.currentTimeMillis() - startTime);
        // Do also fetch for the repository
        fetchRepository(git, remoteRepositoryURI, branch);
    }

    public static void fetchRepository(String path, String remoteURI, String branch) throws IOException, GitAPIException {
        if (!new File(path).exists()) {
            throw new IllegalArgumentException("Cannot use directory: " + path + " no such directory!");
        }
        Git git = Git.init().setDirectory(new File(path)).call();
        fetchRepository(git, remoteURI, branch);
    }

    public static void fetchRepository(Git git, String remoteURI, String branch) throws IOException, GitAPIException {
        FetchCommand fetch = git.fetch();
        fetch.setRemote(remoteURI);
        fetch.setCheckFetchedObjects(true);
        RefSpec refSpec = new RefSpec("+refs/heads/" + branch + ":refs/remotes/origin/" + branch);
        fetch.setRefSpecs(refSpec);
        log.info("Fetching from {} with refspec {}...", remoteURI, refSpec);
        long startTime = System.currentTimeMillis();
        FetchResult result = fetch.call();
        String remoteMessages = result.getMessages();
        if (!remoteMessages.isEmpty()) {
            log.debug("Remote fetch process messages: {}", remoteMessages);
        }
        for (TrackingRefUpdate refUpdate : result.getTrackingRefUpdates()) {
            log.debug("Updated reference: {}, status: {}, new value: {}", new Object[]{refUpdate.getLocalName(),
                refUpdate.getResult(), refUpdate.getNewObjectId()});
        }
        log.info("Fetching done in {}ms. Current FETCH_HEAD is: {}", System.currentTimeMillis() - startTime,
                git.getRepository().resolve("refs/remotes/origin/" + branch).getName());
    }

    public static Iterable<RevCommit> getCommits(Git git, String from, String to) throws IOException, GitAPIException {
        LogCommand log = git.log();
        log.addRange(ObjectId.fromString(from), ObjectId.fromString(to));
        return log.call();
    }

    public static String getFetchHead(String repositoryPath, String branch) throws GitAPIException, AmbiguousObjectException, IOException {
        Git git = Git.init().setDirectory(new File(repositoryPath)).call();
        return git.getRepository().resolve("refs/remotes/origin/" + branch).getName();
    }

    public static List<ChangeFile> getChangeFiles(Git git, RevCommit revCommit, Change change) throws GitAPIException, MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
        List<ChangeFile> changeFiles = new ArrayList<ChangeFile>();
        if (revCommit.getParentCount() == 0) {
            TreeWalk tw = new TreeWalk(git.getRepository());
            tw.reset();
            tw.setRecursive(true);
            tw.addTree(revCommit.getTree());
            while (tw.next()) {
                ChangeFile file = new ChangeFile();
                file.setFilePath(tw.getPathString());
                file.setFileType(ChangeFileType.ADDED);
                changeFiles.add(file);
            }
            tw.release();
        } else {
            RevWalk rw = new RevWalk(git.getRepository());
            RevCommit parent = rw.parseCommit(revCommit.getParent(0).getId());
            DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);

            df.setRepository(git.getRepository());
            df.setDiffComparator(RawTextComparator.DEFAULT);
            df.setDetectRenames(true);

            List<DiffEntry> diffs = df.scan(parent.getTree(), revCommit.getTree());
            for (DiffEntry d : diffs) {
                DiffEntry.ChangeType type = d.getChangeType();
                ChangeFile file = new ChangeFile();
                file.setFilePath(d.getNewPath());
                if (type.equals(DiffEntry.ChangeType.ADD)) {
                    file.setFileType(ChangeFileType.ADDED);
                } else if (type.equals(DiffEntry.ChangeType.DELETE)) {
                    file.setFilePath(d.getOldPath());
                    file.setFileType(ChangeFileType.REMOVED);
                } else if (type.equals(DiffEntry.ChangeType.MODIFY)) {
                    file.setFileType(ChangeFileType.MODIFIED);
                } else if (type.equals(DiffEntry.ChangeType.RENAME)) {
                    file.setFileType(ChangeFileType.RENAMED);
                } else if (type.equals(DiffEntry.ChangeType.COPY)) {
                    file.setFileType(ChangeFileType.COPIED);
                } else {
                    continue;
                }

                file.setChange(change);
                changeFiles.add(file);
            }

            rw.dispose();
        }
        return changeFiles;
    }

    public static String buildGitFullURL(String sshUserName, String url, int port, String projectName) {
        StringBuilder sb = new StringBuilder();
        sb.append("ssh://");
        sb.append(sshUserName);
        sb.append("@");
        sb.append(url);
        sb.append(":");
        sb.append(port);
        sb.append("/");
        sb.append(projectName);
        return sb.toString();
    }
}
