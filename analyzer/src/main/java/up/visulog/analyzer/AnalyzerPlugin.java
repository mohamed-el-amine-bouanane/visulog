package up.visulog.analyzer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import up.visulog.config.Configuration;
import up.visulog.config.PluginConfig;

public abstract class AnalyzerPlugin<T> implements Runnable {
    protected Result<T> result;
    protected PluginConfig options;
    protected Configuration configuration;

    public AnalyzerPlugin(Configuration generalConfiguration, String pluginName) {
        this.options = generalConfiguration.getPluginConfigs().remove(pluginName);
        this.configuration = generalConfiguration;
    }

    interface Result<T> {

        @JsonIgnore
        String getResultAsString();

        /**
         * This is the actual data that is requested by the user
         * It must be something that converts to JSON, or else,
         * the plugin will crash at runtime
         * @return an object that represents the data of the plugin
         */
        @JsonProperty("data")
        T getData();

        /**
         * This is useful in order to know by which plugin the
         * data was generated and to know how to deal with it
         * on the frontend
         * @return the name of the plugin
         */
        @JsonProperty("name")
        String getPluginName();

        /**
         * This is useful in order to know how to render the
         * chart on the front end
         * @return the options of the plugin
         */
        @JsonProperty("options")
        PluginConfig getPluginOptions();

        /**
         * Generates an unique identifier for each requested
         * plugin, in order to differentiate them in the
         * frontend
         * @return a UUID as a string
         */
        @JsonProperty("id")
        String getId();
    }

    /**
     * run this analyzer plugin
     */
    public abstract void run();

    /**
     * @return the result of this analysis. Runs the analysis first if not already done.
     */
    Result<T> getResult() {
        if(result == null) run();
        return result;
    }
}
