package up.visulog.cli;

import java.util.List;
import java.util.Map;

public class ConfigFile {
    public String path;
    public List<PluginConfigFile> plugins;
}

class PluginConfigFile {
    public String name;
    public OptionsConfigFile options;
}

class OptionsConfigFile {
    public List<String> charts;
    public List<String> toggleOptions;
    public Map<String, String> valueOptions;
}