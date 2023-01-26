package up.visulog.config;

import java.io.IOException;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class Configuration {
    private final int port;
    private final Path gitPath;
    private final HashMap<String, PluginConfig> plugins;
    private final boolean indent;
    private final String outputFile;
    private final Repository repo;

    public Configuration(Path gitPath, HashMap<String, PluginConfig> plugins, int port, String outputFile, boolean indent) throws IOException {
        this.gitPath = gitPath;
        this.plugins = new HashMap<String, PluginConfig>(plugins);
        this.indent = indent;
        this.port = port;
        this.outputFile = outputFile;
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        this.repo = repositoryBuilder.setGitDir(new File(this.gitPath.toString() + "/.git"))
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .setMustExist(true)
                .build();
    }

    public Path getGitPath() {
        return gitPath;
    }

    public HashMap<String, PluginConfig> getPluginConfigs() {
        return plugins;
    }

    public int getPort() {
        return this.port;
    }

    public Repository getGitRepo() {
        return this.repo;
    }

    public boolean isIndented() {
        return this.indent;
    }

    public String outputFile() {
        return this.outputFile;
    }
}
