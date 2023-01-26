package up.visulog.analyzer;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import up.visulog.config.Configuration;
import up.visulog.gitrawdata.Commit;
import up.visulog.gitrawdata.Filter;
import up.visulog.config.PluginConfig;


public class CountLinesOverTimePlugin extends AnalyzerPlugin<LinkedList<Lines>> {
    MyResult result; 
    public static final String name = "countLinesOverTime";

    public CountLinesOverTimePlugin(Configuration generalConfiguration) {
        super(generalConfiguration, name);
    }

    MyResult countLinesOverTime(List<Commit> log) {
        var result = new MyResult(this.options);

        class Pair {
            public Calendar date;
            public int addedLines;
            public int deletedLines;

            public Pair(Calendar date, int addedLines, int deletedLines) {
                this.date = date;
                this.addedLines = addedLines;
                this.deletedLines = deletedLines;
            }

            public int geWierdDate() {
                return date.get(Calendar.YEAR) * 1000 + date.get(Calendar.DAY_OF_YEAR);
            }
        }

        ArrayList<Pair> resultList = new ArrayList<Pair>();

        for (var commit : log) {
            var calendarInstace = Calendar.getInstance();
            calendarInstace.setTime(commit.date);
            resultList.add(new Pair(calendarInstace, commit.linesAdded, commit.linesRemoved));
        }

        var groupedByDate = resultList.stream().collect(Collectors.groupingBy(pair -> pair.geWierdDate()));
        var startDate = Calendar.getInstance();
        var endDate = Calendar.getInstance();
        for (var entry : groupedByDate.entrySet()) {
            var added = entry.getValue().stream().mapToInt(pair -> pair.addedLines).sum();
            var deleted = entry.getValue().stream().mapToInt(pair -> pair.deletedLines).sum();
            var date = Calendar.getInstance();

            date.set(Calendar.YEAR, entry.getKey() / 1000);
            date.set(Calendar.DAY_OF_YEAR, entry.getKey() % 1000);
            date.set(Calendar.HOUR, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);

            if(date.before(startDate)) {
                startDate = (Calendar)date.clone();
            } else if(date.after(endDate)) {
                endDate = (Calendar)date.clone();
            }

            result.commits.add(new Lines(added, deleted, date));
        }

        result.commits.sort((line1, line2) -> line1.date.compareTo(line2.date));

        var allDays = new LinkedList<Lines>();
        while(startDate.before(endDate)) {
            allDays.add(new Lines(0, 0, (Calendar)startDate.clone()));
            startDate.add(Calendar.DAY_OF_YEAR, 1);
        }

        var iterator = allDays.iterator();
        int i = 0;
        while(iterator.hasNext() && result.commits.size() > 0) {
            var line = iterator.next();
            if(line.getDate().equals(result.commits.getFirst().getDate())) {
                allDays.set(i, result.commits.removeFirst());
            }
            i++;
        }
        result.commits = allDays;

        return result;
    }

    public void run() {
        List<Filter> filters = Filter.getFilters(this.options.getValueOptions());
        result = countLinesOverTime(Commit.getFilteredCommits(configuration.getGitRepo(), filters));
    }

    public MyResult getResult() {
        if (result == null) run();
        return result;
    }

    static class MyResult implements AnalyzerPlugin.Result<LinkedList<Lines>> {
        LinkedList<Lines> commits;
        private PluginConfig options;

        MyResult(PluginConfig options) {
            this.commits = new LinkedList<Lines>();
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
            return CountLinesOverTimePlugin.name;
        }

        public MyResult() {
            commits = new LinkedList<Lines>();
        }

        @Override
        public String getResultAsString() {
            return commits.toString();
        }

        @Override
        public LinkedList<Lines> getData() {
            var list = new LinkedList<Lines>(commits);
            return list;
        }
    }
}

class Lines {
    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    public int added;
    public int deleted;

    @JsonIgnore
    public Calendar date;

    public Lines(int added, int deleted, Calendar date) {
        this.added = added;
        this.deleted = deleted;
        this.date = date;
    }

    @JsonProperty("date")
    public String getDate() {
        return df.format(date.getTime());
    }
}
