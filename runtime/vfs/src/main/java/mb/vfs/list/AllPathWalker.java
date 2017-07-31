package mb.vfs.list;

import mb.vfs.path.PPath;

public class AllPathWalker implements PathWalker {
    private static final long serialVersionUID = 1L;


    @Override public boolean traverse(PPath path, PPath root) {
        return true;
    }

    @Override public boolean matches(PPath path, PPath root) {
        return true;
    }


    @Override public int hashCode() {
        return 0;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        return true;
    }

    @Override public String toString() {
        return "AllPathWalker";
    }
}
