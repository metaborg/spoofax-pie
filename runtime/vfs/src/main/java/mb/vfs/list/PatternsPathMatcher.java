package mb.vfs.list;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import mb.vfs.path.PPath;

public class PatternsPathMatcher implements PathMatcher {
    private static final long serialVersionUID = 1L;

    private final Set<AntPattern> patterns;


    public PatternsPathMatcher(Collection<String> patterns) {
        this.patterns = new HashSet<>();
        for(String pattern : patterns) {
            this.patterns.add(new AntPattern(pattern));
        }
    }

    public PatternsPathMatcher(String pattern) {
        this.patterns = new HashSet<>();
        this.patterns.add(new AntPattern(pattern));
    }


    @Override public boolean matches(PPath path, PPath root) {
        final PPath relativePath = root.normalized().relativizeFrom(path.normalized());
        for(AntPattern pattern : patterns) {
            if(pattern.match(relativePath.toString())) {
                return true;
            }
        }
        return false;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + patterns.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final PatternsPathMatcher other = (PatternsPathMatcher) obj;
        if(!patterns.equals(other.patterns))
            return false;
        return true;
    }

    @Override public String toString() {
        return "PatternsPathMatcher";
    }
}
