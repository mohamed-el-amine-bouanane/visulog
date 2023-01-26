package up.visulog.analyzer;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;


import up.visulog.config.Configuration;
import up.visulog.gitrawdata.Commit;
import up.visulog.gitrawdata.Filter;
import up.visulog.config.PluginConfig;


public class CountAuthorsPlugin extends AnalyzerPlugin<Integer> {
    public static final String name = "countAuthors";

    public CountAuthorsPlugin(Configuration generalConfiguration) {
        super(generalConfiguration, name);
    }

    MyResult countAuthors(List<Commit> log) {
        var result = new MyResult(this.options);

        for (var commit : log) {
            result.authorSet.add(commit.author);
        }
        return result;
    }

    public void run() {
        List<Filter> filters = Filter.getFilters(this.options.getValueOptions());
        result = countAuthors(Commit.getFilteredCommits(configuration.getGitRepo(), filters));
    }

    static class MyResult implements AnalyzerPlugin.Result<Integer> {
        HashSet<String> authorSet;
        private PluginConfig options;

        MyResult(PluginConfig options) {
            this.authorSet = new HashSet<String> ();
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
            return CountAuthorsPlugin.name;
        }

        public MyResult() {
            authorSet = new HashSet<String>();
        }

        @Override
        public String getResultAsString() {
            return authorSet.toString();
        }

        @Override
        public Integer getData() {
            return this.authorSet.size();
        }

    }
}
