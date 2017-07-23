package mb.vfs.list;

import mb.vfs.path.PPath;

import java.nio.file.Files;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ExtensionsPathWalker implements PathWalker {
    private static final long serialVersionUID = 1L;

    private final Set<String> extensions;

    public ExtensionsPathWalker(Collection<String> extensions) {
        this.extensions = new HashSet<>(extensions);
    }

    @Override public boolean matches(PPath path) {
        if(!Files.isRegularFile(path.getJavaPath())) {
            return false;
        }
        final String extension = path.extension();
        if(extension == null) {
            return false;
        }
        return extensions.contains(extension);
    }

    @Override public boolean traverse(PPath path) {
        return true;
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
        final ExtensionsPathWalker other = (ExtensionsPathWalker) obj;
        if(!extensions.equals(other.extensions))
            return false;
        return true;
    }

    @Override public String toString() {
        return "ExtensionsPathWalker";
    }
}
