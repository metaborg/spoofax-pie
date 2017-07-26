package mb.vfs.list;

import mb.vfs.path.PPath;

public class PatternPathMatcher implements PathMatcher {
    private static final long serialVersionUID = 1L;

    private final AntPattern pattern;


    public PatternPathMatcher(String pattern) {
        this.pattern = new AntPattern(pattern);
    }


    @Override public boolean matches(PPath path) {
        return pattern.match(path.toString());
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + pattern.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final PatternPathMatcher other = (PatternPathMatcher) obj;
        if(!pattern.equals(other.pattern))
            return false;
        return true;
    }

    @Override public String toString() {
        return "PatternPathMatcher";
    }
}
