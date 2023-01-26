package up.visulog.gitrawdata;

import java.util.ArrayList;
import java.util.Map;
import java.text.ParseException;

public abstract class Filter {
    public abstract boolean filter(Commit commit);

    public static Filter getFilter(String filterType, String filterValue) throws IllegalArgumentException, ParseException {
        if (filterType.equals("author")) {
            return new NameFilter(filterValue);
        } else if (filterType.equals("date")) {
            return new DateFilter(filterValue);
        } else {
            throw new IllegalArgumentException("Invalid filter type: " + filterType);
        }
    }

    public static ArrayList<Filter> getFilters(Map<String, String> filtersSet){
        ArrayList<Filter> filters = new ArrayList<Filter>();
        for (var options : filtersSet.entrySet()){
            try {
                filters.add(Filter.getFilter(options.getKey(), options.getValue()));
            } catch (IllegalArgumentException e) {
            } catch (ParseException e) {
                System.out.println("Invalid value for filter \"" + options.getKey() + "\": " + options.getValue());
            }
        }
        return filters;
    }
}
