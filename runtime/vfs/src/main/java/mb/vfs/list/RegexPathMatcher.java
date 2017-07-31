package mb.vfs.list;

import java.util.regex.Pattern;

import mb.vfs.path.PPath;

public class RegexPathMatcher implements PathMatcher {
    private static final long serialVersionUID = 1L;

    private final Pattern regex;


    public RegexPathMatcher(Pattern regex) {
        this.regex = regex;
    }
    
    public RegexPathMatcher(String regex) {
        this.regex = Pattern.compile(regex);
    }


    @Override public boolean matches(PPath path, PPath root) {
        return regex.matcher(path.toString()).matches();
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + regex.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final RegexPathMatcher other = (RegexPathMatcher) obj;
        if(!regex.equals(other.regex))
            return false;
        return true;
    }

    @Override public String toString() {
        return "RegexPathMatcher";
    }
}
