package up.visulog.cli;

import static spark.Spark.*;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.stream.Collectors;

public class ServeFrontend {
    public static void serve(int port, String data) throws IOException {
        port(port);
        System.out.printf("Server started on http://localhost:%d/", port);
        get("/", (req, res) -> {
            res.header("Content-Type", "text/html");
            return getIndex();
        });

        get("/style.css", (req, res) -> {
            res.header("Content-Type", "text/css");
            return getStyle();
        });

        get("/script.js", (req, res) -> {
            res.header("Content-Type", "application/javascript");
            return getScript();
        });

        get("/data.json", (req, res) -> {
            res.header("Content-Type", "application/json");
            return data;
        });
    }
    
    private static String getIndex() throws IOException {
        return getResourceFileAsString("module.html");
    }
    
    private static String getScript() throws IOException {
        return getResourceFileAsString("script.js");
    }

    private static String getStyle() throws IOException {
        return getResourceFileAsString("style.css");
    }
    /**
     * Reads given resource file as a string.
     *
     * @param fileName path to the resource file
     * @return the file's contents
     * @throws IOException if read fails for any reason
     */
    static String getResourceFileAsString(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null) return null;
            try (InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }
}
