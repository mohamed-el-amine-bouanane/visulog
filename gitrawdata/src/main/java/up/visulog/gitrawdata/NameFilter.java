package up.visulog.gitrawdata;

public class NameFilter extends Filter {
    private String regexName;

    public NameFilter(String name){
        regexName = name;
    }

    public boolean filter(Commit commit){
        return commit.author.matches(regexName);
    }
}
