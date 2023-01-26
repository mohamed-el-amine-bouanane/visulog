package up.visulog.gitrawdata;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFilter extends Filter {
    private Date dateEnd;
    private Date dateStart;
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    public DateFilter(String start, String end) throws ParseException {
        dateStart = df.parse(start);
        dateEnd = df.parse(end);
    }

    public DateFilter(String filterValue) throws ParseException {
        String[] date = filterValue.split("~");
        if (date.length == 1) {
            dateStart = df.parse(date[0]);
            dateEnd = new Date(dateStart.getTime() + 1000 * 60 * 60 * 24);
        } else if(date.length == 2) {
            dateStart = df.parse(date[0]);
            dateEnd = df.parse(date[1]);
        } else {
            throw new ParseException("Invalid date format", 0);
        }
    }

    public boolean filter(Commit commit){
        if (commit.date.compareTo(dateStart)>=0 && commit.date.compareTo(dateEnd)<=0) return true;
        return false;
    }
}
