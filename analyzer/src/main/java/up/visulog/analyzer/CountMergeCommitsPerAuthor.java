package up.visulog.analyzer;

import up.visulog.config.Configuration;
import up.visulog.config.PluginConfig;
import up.visulog.gitrawdata.Commit;
import up.visulog.gitrawdata.Filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CountMergeCommitsPerAuthor extends AnalyzerPlugin<Map<String, Integer>> {
    public static final String name = "countMergeCommits";

    public CountMergeCommitsPerAuthor(Configuration generalConfiguration) {
        super(generalConfiguration, name);
    }

    Result processLog(List<Commit> gitLog) {
        var result = new Result(this.options);
        for (var commit : gitLog) {
            if (commit.mergeCommit) {
            	var nb = result.commitsPerAuthor.getOrDefault(commit.author, 0);
            	result.commitsPerAuthor.put(commit.author, nb + 1); 
            }
        }
        return result;
    }

    public void run() {
        List<Filter> filters = Filter.getFilters(this.options.getValueOptions());
        result = processLog(Commit.getFilteredCommits(configuration.getGitRepo(), filters));
    }

    static class Result implements AnalyzerPlugin.Result<Map<String, Integer>> {
        private PluginConfig options;
        private final Map<String, Integer> commitsPerAuthor = new HashMap<>(); 

        Result(PluginConfig options) {
            this.options = options;
        }

        @Override
        public String getPluginName() {
            return CountMergeCommitsPerAuthor.name;
        }

        @Override
        public String getId() {
            var uuid = UUID.randomUUID().toString();
            return uuid;
        }

        @Override
        public PluginConfig getPluginOptions() {
            return this.options;
        }

        @Override
        public String getResultAsString() {
            return commitsPerAuthor.toString();
        }

        @Override
        public Map<String, Integer>getData() {
            return new HashMap<>(commitsPerAuthor);
        }
    }
}

