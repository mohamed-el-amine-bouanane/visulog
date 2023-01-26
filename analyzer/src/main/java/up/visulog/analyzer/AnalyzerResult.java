package up.visulog.analyzer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class AnalyzerResult {
    public List<AnalyzerPlugin.Result<?>> getSubResults() {
        return subResults;
    }

    private final List<AnalyzerPlugin.Result<?>> subResults;

    public AnalyzerResult(List<AnalyzerPlugin.Result<?>> subResults) {
        this.subResults = subResults;
    }

    @Override
    public String toString() {
        return subResults
            .stream()
            .map(AnalyzerPlugin.Result::getResultAsString)
            .reduce("", (acc, cur) -> acc + "\n" + cur);
    }

    /**
     * Helper function for JSON generation
     * @param identation whether to generate JSON with identation or not
     * @return a stringified JSON
     */
    private String _JSON(boolean identation) {
        try {
            var mapper = new ObjectMapper();
            if(identation) {
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
            }
            return mapper.writeValueAsString(
                subResults
                    .stream()
                    .collect(Collectors.toList())
            );
        } catch(JsonProcessingException e) {
            System.err.println("Error while stringifying JSON");
            e.printStackTrace();
            System.exit(1);
        }
        return "[]";
    }

    /**
     * @param identation whether to generate JSON with identation or not
     * @return an idented stringified JSON containing
     * the data of all plugins
     */
    public String toJSON(boolean identation) {
        return _JSON(identation);
    }

    /**
     * Outputs the data of all plugins to the given file
     * @param filename the name of the file the JSON will be outputed to
     * @param identation whether to generate JSON with identation or not
     * @throws IOException in case the program can't write to the file
     */
    public void toJSONFile(String filename, boolean identation) {
        try {
            FileWriter file = new FileWriter(filename);
            file.write(this._JSON(identation));
            file.close();
        } catch(IOException e) {
            System.err.println("Could not save JSON to file, printing to stderr and exiting instead");
            System.err.println(this.toJSON(true));
            e.printStackTrace();
            System.exit(1);
        }
    }
}
