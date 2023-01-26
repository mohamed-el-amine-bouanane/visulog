package up.visulog.gitrawdata;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Date;
import java.util.List;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;

import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

public class Commit {
    private static List<RevCommit> rCommits = null;
    private static HashMap<String, Commit> cache = new HashMap<String, Commit>();
    public final String id;
    public final Date date;
    public final String author;
    public final String description;
    public final boolean mergeCommit;
    public final int linesAdded;
    public final int linesRemoved;
    public final HashMap<String, Integer> files;
    private static DiffFormatter df = null;
    private static RevWalk rw = null;

    public Commit(String id, String author, Date date, String description, boolean mergeCommit, int linesAdded, int linesRemoved, HashMap<String, Integer> files) {
        this.id = id;
        this.author = author;
        this.date = date;
        this.description = description;
        this.mergeCommit = mergeCommit;
        this.linesAdded = linesAdded;
        this.linesRemoved = linesRemoved;
        this.files = files;
    }

    @Override
    public String toString() {
        return "Commit{" +
                "id='" + id + '\'' +
                ", date='" + date + '\'' +
                ", author='" + author + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
    /**
     * Transform a JGit revCommit into a regular Commit object.
     * @throws IOException
     * @throws IncorrectObjectTypeException
     * @throws MissingObjectException
     */

    public static Commit commitOfRevCommit (AnyObjectId id, RevCommit rCommit, Repository repo, boolean calculateDiff) throws MissingObjectException, IncorrectObjectTypeException, IOException{
        if(cache.get(id.getName()) != null) {
            return cache.get(id.getName());
        }
        var author = rCommit.getAuthorIdent();
        var name = author.getName();
        var email = author.getEmailAddress();

        // get LocalDateTime of commit
        var instant = Instant.ofEpochSecond(rCommit.getCommitTime());
        var date = Date.from(instant);

        // Getting the number of added/deleted lines
        // https://stackoverflow.com/questions/19467305/using-the-jgit-how-can-i-retrieve-the-line-numbers-of-added-deleted-lines
        int linesDeleted = 0;
        int linesAdded = 0;
        HashMap<String, Integer> files = new HashMap<>();
        boolean mergeCommit = rCommit.getParentCount() > 1 ? true : false;
        if(calculateDiff && !mergeCommit) {
            RevCommit parent = rCommit.getParentCount() == 0 ? null : rw.parseCommit(rCommit.getParent(0).getId());
            List<DiffEntry> diffs = df.scan(parent == null ? null : parent.getTree(), rCommit.getTree());
            for (DiffEntry diff : diffs) {
                String newpath = diff.getNewPath();
                Path path = Paths.get(newpath);
                String filename = path.getFileName().toString();
                for (Edit edit : df.toFileHeader(diff).toEditList()) {
                    linesDeleted += edit.getLengthA();
                    linesAdded += edit.getLengthB();
                }
                files.put(filename, linesAdded+linesDeleted);
            }
        }

        var commit =
            new Commit(id.getName(),
                name + " <" + email+">",
                date,
                rCommit.getFullMessage(),
                mergeCommit,
                linesAdded,
                linesDeleted,
                files);
        if(calculateDiff && !mergeCommit) {
            cache.put(commit.id, commit);
        }
        return commit;
    }


    /**
     * Parses a log item and outputs a commit object. Exceptions will
     * be thrown in case the input does not have the proper format.
     */
    public static Commit parse (Repository repo, AnyObjectId id)
	throws MissingObjectException,
	       IncorrectObjectTypeException,
	       IOException {
        try (RevWalk walk = new RevWalk(repo)) {
            RevCommit rCommit = walk.parseCommit(id);
            walk.dispose();
            return commitOfRevCommit(id, rCommit, repo, true);
        }
    }

    public static List<Commit> getFilteredCommits(Repository repo, List<Filter> filters){
        if(df == null) {
            df = new DiffFormatter(DisabledOutputStream.INSTANCE);
            df.setRepository(repo);
            df.setDiffComparator(RawTextComparator.DEFAULT);
            df.setDetectRenames(true);
        }
        if(rw == null) {
            rw = new RevWalk(repo);
        }
        List<Commit> res = getAllCommitsWithoutDiff(repo);
        System.out.println("[Filtering commits]: done !");
        int size = res.size();
        res.removeIf(commit -> {
            boolean keep = true;
            for (Filter filter : filters) {
                if(!filter.filter(commit)){
                    keep = false;
                    break;
                }
            }
            return !keep;
        });
        System.out.println("[Filtering commits]: done !");
        System.out.println("[Filtering commits]: commits before filters: " + size);
        System.out.println("[Filtering commits]: commits after filters: " + res.size());
        return getCommitsFromList(repo, res.stream().map(commit -> commit.id).collect(Collectors.toList()));
    }

    public static List<Commit> getAllCommitsWithoutDiff(Repository repo) {
        try {
            Git git = new Git(repo);
            var rCommitsList = getRevCommits(repo);
            System.out.println("[Getting all commits without diff]: starting...");
            List<Commit> commits = new ArrayList<>();
            for(var rCommit : rCommitsList) {
                commits.add(commitOfRevCommit(rCommit.getId(), rCommit, repo, false));
            }
            System.out.println("[Getting all commits without diff]: done !");
            git.close();
            return commits;
        } catch (RevisionSyntaxException  | IOException  e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    public static List<Commit> getCommitsFromList(Repository repo, List<String> hashes) {
        try {
            List<Commit> commits = new ArrayList<Commit>();
            Git git = new Git(repo);
            var rCommitsList = getRevCommits(repo);
            System.out.println("[Getting all commits with diff]: starting...");
            Integer counter = 0;
            int size = hashes.size();
            HashSet<String> hashesSet = new HashSet<>(hashes); // Using this hashset tremendously speeds up the process
            for(var rCommit : rCommitsList) {
                if(hashesSet.contains(rCommit.getName())) {
                    counter++;
                    System.out.printf("[%s%s] | %d/%d\r", "=".repeat((int)(((double)counter) / ((double)size) * 20)), " ".repeat(20 - (int)(((double)counter) / ((double)size) * 20)), counter, size);
                    commits.add(commitOfRevCommit(rCommit.getId(), rCommit, repo, true));
                }
            }
            System.out.println("[Getting all commits with diff]: done !");
            git.close();
            return commits;
        } catch (RevisionSyntaxException | IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    private static List<RevCommit> getRevCommits(Repository repo) {
        if(Commit.rCommits != null) return Commit.rCommits;
        System.out.println("[Caching commits]: starting...");
        try {
            Git git = new Git(repo);
            Iterable<RevCommit> rCommits = git.log().all().call();
            List<RevCommit> rCommitsList = new ArrayList<>();
            rCommits.forEach(rc -> rCommitsList.add(rc));
            Commit.rCommits = rCommitsList;
            git.close();
            System.out.println("[Caching commits]: done !");
            return new ArrayList<RevCommit>(rCommitsList);
        } catch (RevisionSyntaxException | IOException | GitAPIException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
}
