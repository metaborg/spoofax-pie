package mb.vfs.list;

import mb.vfs.path.PPath;

public class PathMatcherWalker implements PathWalker {
    private static final long serialVersionUID = 1L;

    private final PathMatcher matcher;


    public PathMatcherWalker(PathMatcher matcher) {
        this.matcher = matcher;
    }


    @Override public boolean traverse(PPath path, PPath root) {
        return true;
    }

    @Override public boolean matches(PPath path, PPath root) {
        return matcher.matches(path, root);
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + matcher.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final PathMatcherWalker other = (PathMatcherWalker) obj;
        if(!matcher.equals(other.matcher))
            return false;
        return true;
    }

    @Override public String toString() {
        return "PathMatcherWalker(" + matcher.toString() + ")";
    }
}
