package up.visulog.analyzer;

import up.visulog.config.Configuration;
import up.visulog.config.PluginConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;
import java.util.stream.Collectors;

public class Analyzer {
    private final Configuration config;

    public Analyzer(Configuration config) {
        this.config = config;
    }

    public AnalyzerResult computeResults() {
        List<AnalyzerPlugin<?>> plugins = new ArrayList<>();

        var pluginsConfig = new ConcurrentHashMap<String, PluginConfig>(config.getPluginConfigs());

        pluginsConfig.forEach((k, v) -> {
            var pluginName = k;
            var pluginConfig = v;
            var plugin = makePlugin(pluginName, pluginConfig);
            plugin.ifPresent(plugins::add);
        });

        var threadList = new ArrayList<Thread>();

        // run all the plugins
        for (var plugin: plugins) {
            plugin.run();
        }

        // store the results together in an AnalyzerResult instance and return it
        return new AnalyzerResult(plugins.stream().map(AnalyzerPlugin::getResult).collect(Collectors.toList()));
    }

    private Optional<AnalyzerPlugin<?>> makePlugin(String pluginName, PluginConfig pluginConfig) {
        switch (pluginName) {
            case CountCommitsPerAuthorPlugin.name : return Optional.of(new CountCommitsPerAuthorPlugin(config));
            case CountAuthorsPlugin.name : return Optional.of(new CountAuthorsPlugin(config));
            case CountLinesPerAuthorPlugin.name : return Optional.of(new CountLinesPerAuthorPlugin(config));
            case CountLinesRemovedPerAuthorPlugin.name : return Optional.of(new CountLinesRemovedPerAuthorPlugin(config));
            case CountMergeCommitsPerAuthor.name : return Optional.of(new CountMergeCommitsPerAuthor(config));
            case CountContributionPercentagePlugin.name : return Optional.of(new CountContributionPercentagePlugin(config));
            case CountLinesOverTimePlugin.name : return Optional.of(new CountLinesOverTimePlugin(config));
            case CountFilesChanged.name : return Optional.of(new CountFilesChanged(config));
            case CountAverageLinesPerCommitPerAuthor.name : return Optional.of(new CountAverageLinesPerCommitPerAuthor(config));
            default : return Optional.empty();
        }
    }

}
