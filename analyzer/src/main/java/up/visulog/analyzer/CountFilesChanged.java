package up.visulog.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import up.visulog.config.Configuration;
import up.visulog.config.PluginConfig;
import up.visulog.gitrawdata.Commit;
import up.visulog.gitrawdata.Filter;

public class CountFilesChanged extends AnalyzerPlugin<Map<String, Integer>>{
    public static final String name = "countFilesChanged";
    private Result result;
    
    public CountFilesChanged(Configuration config){
        super(config, name);
    }

    Result processLog(List<Commit> gitLog) {
        var result = new Result(this.options);
        for(Commit c : gitLog) {
            c.files.forEach((k, v) -> {
                if(!result.files.containsKey(k)){
                    result.files.put(k, v);
                } else {
                    result.files.replace(k, v+result.files.get(k));
                }
            });
        }
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
        private PluginConfig options;
        private HashMap<String, Integer> files;

        Result(PluginConfig options) {
            files = new HashMap<>();
            this.options = options;
        }

        @Override
        public String getPluginName() {
            return CountFilesChanged.name;
        }

        @Override
        public String getId() {
            var uuid = UUID.randomUUID().toString();
            return uuid;
        }

        @Override
        public PluginConfig getPluginOptions() {
            return options;
        }

        @Override
        public String getResultAsString() {
            return files.toString();
        }

        @Override
        public Map<String, Integer> getData() {
            return new HashMap<String, Integer>(files);
        }
    
    }

}
