package up.visulog.analyzer;

import up.visulog.config.Configuration;
import up.visulog.config.PluginConfig;
import up.visulog.gitrawdata.Commit;
import up.visulog.gitrawdata.Filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CountAverageLinesPerCommitPerAuthor extends AnalyzerPlugin<Map<String, Integer>> {
    public static final String name = "countLinesPerCommitPerAuthor";
    private Result result;

    public CountAverageLinesPerCommitPerAuthor(Configuration config) {
        super(config, name);
    }

    Result processLog (List<Commit> log) {
        var result = new Result(this.options);
        HashMap<String, Integer> authors = new HashMap<>();
        for (var commit : log) {
            if(authors.containsKey(commit.author) && !commit.mergeCommit) {
                authors.replace(commit.author, authors.get(commit.author)+1);
                result.averageLines.replace(commit.author, result.averageLines.get(commit.author)+
                                                            commit.linesAdded+
                                                            commit.linesRemoved);
            } else if(!commit.mergeCommit) {
                authors.put(commit.author, 1);
                result.averageLines.put(commit.author, commit.linesAdded+commit.linesRemoved);
            }
        }
        authors.forEach((s, i) ->
            result.averageLines.replace(s, (result.averageLines.get(s))/i)
        );

        return result;
    }

    @Override
    public void run() {
        List<Filter> filters = Filter.getFilters(this.options.getValueOptions());
        result = processLog(Commit.getFilteredCommits(configuration.getGitRepo(), filters));
    }

    @Override
    public Result getResult() {
        if (result == null) run();
        return result;
    }

    static class Result implements AnalyzerPlugin.Result<Map<String, Integer>> {
        private HashMap<String, Integer> averageLines;
        private PluginConfig options;

        Result(PluginConfig options) {
            this.averageLines = new HashMap<String, Integer>();
            this.options = options;
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
        public String getPluginName() {
            return CountAverageLinesPerCommitPerAuthor.name;
        }

        @Override
        public String getResultAsString() {
            return averageLines.toString();
        }

        @Override
        public Map<String, Integer> getData() {
            return new HashMap<String, Integer>(averageLines);
        }

    }
}
