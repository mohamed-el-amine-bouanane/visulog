package up.visulog.analyzer;

import up.visulog.config.Configuration;
import up.visulog.config.PluginConfig;
import up.visulog.gitrawdata.Commit;
import up.visulog.gitrawdata.Filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CountContributionPercentagePlugin extends AnalyzerPlugin<Map<String, Double>> {
    public static final String name = "countContributionPercentage";

    public CountContributionPercentagePlugin(Configuration generalConfiguration) {
        super(generalConfiguration, name);
    }

    Result processLog(List<Commit> gitLog) {
        var result = new Result(this.options);
        var totalCommits = gitLog.size();
        for (var commit : gitLog) {
            var commits = result.percentagePerAuthor.getOrDefault(commit.author, 0.0);
            result.percentagePerAuthor.put(commit.author, commits + 1);
        }
        for(var author : result.percentagePerAuthor.keySet()) {
            var commits = result.percentagePerAuthor.get(author);
            result.percentagePerAuthor.put(author, commits / totalCommits * 100);
        }
        return result;
    }

    public void run() {
        List<Filter> filters = Filter.getFilters(this.options.getValueOptions());
        result = processLog(Commit.getFilteredCommits(configuration.getGitRepo(), filters));
    }

    static class Result implements AnalyzerPlugin.Result<Map<String, Double>> {
        private PluginConfig options;
        private final Map<String, Double> percentagePerAuthor = new HashMap<>();

        Result(PluginConfig options) {
            this.options = options;
        }

        @Override
        public String getPluginName() {
            return CountContributionPercentagePlugin.name;
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
            return percentagePerAuthor.toString();
        }

        @Override
        public Map<String, Double>getData() {
            return new HashMap<String, Double>(percentagePerAuthor);
        }
    }
}
