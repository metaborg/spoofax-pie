package mb.vfs.list;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import mb.vfs.path.PPath;

public class ExtensionsPathMatcher implements PathMatcher {
    private static final long serialVersionUID = 1L;

    private final Set<String> extensions;


    public ExtensionsPathMatcher(Collection<String> extensions) {
        this.extensions = new HashSet<>(extensions);
    }

    public ExtensionsPathMatcher(String extension) {
        this.extensions = new HashSet<>();
        this.extensions.add(extension);
    }


    @Override public boolean matches(PPath path, PPath root) {
        if(!path.isFile()) {
            return false;
        }
        final String extension = path.extension();
        if(extension == null) {
            return false;
        }
        return extensions.contains(extension);
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + extensions.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final ExtensionsPathMatcher other = (ExtensionsPathMatcher) obj;
        if(!extensions.equals(other.extensions))
            return false;
        return true;
    }

    @Override public String toString() {
        return "ExtensionsPathMatcher";
    }
}
