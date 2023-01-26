package up.visulog.cli;

import up.visulog.analyzer.Analyzer;
import up.visulog.analyzer.CountCommitsPerAuthorPlugin;
import up.visulog.analyzer.CountContributionPercentagePlugin;
import up.visulog.analyzer.CountLinesOverTimePlugin;
import up.visulog.analyzer.CountFilesChanged;
import up.visulog.analyzer.CountLinesPerAuthorPlugin;
import up.visulog.analyzer.CountLinesRemovedPerAuthorPlugin;
import up.visulog.analyzer.CountMergeCommitsPerAuthor;
import up.visulog.analyzer.CountAuthorsPlugin;
import up.visulog.analyzer.CountAverageLinesPerCommitPerAuthor;
import up.visulog.config.Configuration;
import up.visulog.config.PluginConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;


class Arguments {
    @Parameter(description = "Git path")
    private String gitPath = "";

    @Parameter(names = { "-p", "--addPlugin" }, description = "Selectionner un plugin a ajouter")
    private List<String> plugins;

    @Parameter(names = { "-c", "--loadConfigFile" }, description = "Charger un fichier de configuration")
    private String configFile = "";

    @Parameter(names = { "-s", "--justSaveConfigFile" }, description = "Charger un fichier de configuration")
    private boolean justSave;

    @Parameter(names = { "-i", "--indent" }, description = "Indenter l'affichage")
    private boolean indentation = false;

    @Parameter(names = { "-o", "--output" }, description = "Mettre le contenu dans un fichier")
    private String outputFile = "";

    @Parameter(names = { "--port", "--serve" }, description = "Servir le contenu sur un port")
    private int port = -1;

    @Parameter(names = {"--h", "--help"}, help = true, description = "Affice ce message")
    private boolean help = false;



    public List<String> getPlugins() {
        if(this.plugins != null)
            return new ArrayList<String>(this.plugins);
        else
            return new ArrayList<String>();
    }

    public String getGitPath() {
        return this.gitPath;
    }

    public String getConfigFile() {
        return this.configFile;
    }

    public int getPort() {
        return this.port;
    }

    public String getOutputFile() {
        return this.outputFile;
    }

    public boolean isIndented() {
        return this.indentation;
    }

    public boolean isHelp() {
        return this.help;
    }
}

public class CLILauncher {
    public static void main(String[] args) {
        var config = makeConfigFromCommandLineArgs(args);
        if (config.isPresent()) {
            var analyzer = new Analyzer(config.get());
            var results = analyzer.computeResults();
            if(config.get().getPort() != -1) {
                try {
                    ServeFrontend.serve(config.get().getPort(), results.toJSON(false));
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            } else if(!config.get().outputFile().equals("")) {
                results.toJSONFile(config.get().outputFile(), config.get().isIndented());
            } else {
                System.out.println(results.toJSON(config.get().isIndented()));
            }
        } else displayHelpAndExit();
    }

    static Optional<Configuration> makeConfigFromCommandLineArgs(String[] args) {
        var plugins = new HashMap<String, PluginConfig>();
        var arguments = new Arguments();
        Object yamlObject = new Object();
        try {
            var jct = JCommander.newBuilder()
                .addObject(arguments)
                .build();
            jct.parse(args);
            if(arguments.isHelp()) {
                jct.usage();
                System.exit(0);
            }
        } catch (ParameterException e){
            displayHelpAndExit();
        }

        Path path = Paths.get(".");

        String configFile = arguments.getConfigFile();

        File defaultConfig = new File("./.visulog.yml");
        boolean isConfigVisulog = defaultConfig.isFile();

        if(configFile.equals("") && isConfigVisulog) {
            configFile = "./" + defaultConfig.getName();
        }

        if(!configFile.equals("")) {
            try {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                File myObj = new File(configFile);
                Scanner myReader = new Scanner(myObj);
                String yamlFile = "";
                while (myReader.hasNextLine()) {
                    yamlFile += myReader.nextLine() + "\n";
                }
                myReader.close();
                yamlObject = mapper.readValue(yamlFile, ConfigFile.class);
                ConfigFile cfg = (ConfigFile)yamlObject;
                if(cfg.path != null) {
                    path = Paths.get(cfg.path);
                }

                if(!cfg.plugins.isEmpty()){
                    for(var plugin : cfg.plugins){
                        var pluginConfig = new PluginConfig();
                        if(plugin.options != null) {
                            if(plugin.options.charts != null) {
                                for(var chart : plugin.options.charts){
                                    pluginConfig.addChart(chart);
                                }
                            }
                            if(plugin.options.valueOptions != null) {
                                for(var option : plugin.options.valueOptions.entrySet()){
                                    pluginConfig.addValueOption(option.getKey(), option.getValue());
                                }
                            }
                            if(plugin.options.toggleOptions != null) {
                                for(var option : plugin.options.toggleOptions){
                                    pluginConfig.addToggledOption(option);
                                }
                            }
                        }
                        plugins.put(plugin.name, pluginConfig);
                    }
                }
            } catch (JsonMappingException e){
                e.printStackTrace();
                System.exit(1);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        if(!arguments.getGitPath().equals("")) {
            path = Paths.get(arguments.getGitPath());
        }

        for (var plugin : arguments.getPlugins()) {
            switch (plugin) {
                case "countCommits":
                    plugins.put(CountCommitsPerAuthorPlugin.name, new PluginConfig().addChart("bars"));
                    break;
                case "countAuthors":
                    plugins.put(CountAuthorsPlugin.name, new PluginConfig().addChart("bars"));
                    break;
                case "countLines":
                    plugins.put(CountLinesPerAuthorPlugin.name, new PluginConfig().addChart("bars"));
                    break;
                case "countLinesRemoved":
                    plugins.put(CountLinesRemovedPerAuthorPlugin.name, new PluginConfig().addChart("bars"));
                    break;
                case "countMergeCommits":
                    plugins.put(CountMergeCommitsPerAuthor.name, new PluginConfig().addChart("bars"));
                    break;
                case "countContributionPercentage":
                    plugins.put(CountContributionPercentagePlugin.name, new PluginConfig().addChart("bars"));
                    break;
                case "countLinesOverTime":
                    plugins.put(CountLinesOverTimePlugin.name, new PluginConfig().addChart("bars"));
                    break;
                case "countFilesChanged" : 
                    plugins.put(CountFilesChanged.name, new PluginConfig().addChart("bars"));
                case "CountAverageLinesPerCommitPerAuthor":
                    plugins.put(CountAverageLinesPerCommitPerAuthor.name, new PluginConfig().addChart("bars"));
                    break;
            }
        }
        try {
            return Optional.of(new Configuration(path, plugins, arguments.getPort(), arguments.getOutputFile(), arguments.isIndented()));
        } catch (IOException e) {
            System.out.println("Git repository not found");
            e.printStackTrace();
            System.exit(1);
            return Optional.empty();
        }
    }

    private static void displayHelpAndExit() {
        var jct = JCommander.newBuilder()
            .addObject(new Arguments())
            .build();
        jct.usage();
        System.exit(0);
    }
}
