package mb.vfs.list;

import mb.vfs.path.PPath;

public class DirectoryPathWalker implements PathWalker {
    private static final long serialVersionUID = 1L;

    private final boolean ignoreHidden;


    public DirectoryPathWalker(boolean ignoreHidden) {
        this.ignoreHidden = ignoreHidden;
    }


    @Override public boolean traverse(PPath path, PPath root) {
        if(ignoreHidden) {
            final PPath leaf = path.leaf();
            if(leaf != null && leaf.toString().startsWith(".")) {
                return false;
            }
        }
        return true;
    }

    @Override public boolean matches(PPath path, PPath root) {
        if(!path.isDir()) {
            return false;
        }
        if(ignoreHidden) {
            final PPath leaf = path.leaf();
            if(leaf != null && leaf.toString().startsWith(".")) {
                return false;
            }
        }
        return true;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (ignoreHidden ? 1231 : 1237);
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final DirectoryPathWalker other = (DirectoryPathWalker) obj;
        if(ignoreHidden != other.ignoreHidden)
            return false;
        return true;
    }

    @Override public String toString() {
        return "DirectoryPathWalker";
    }
}
