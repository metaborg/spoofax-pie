package mb.pipe.run.core.path;

import java.nio.file.Files;

class DirectoryPathMatcher implements PathMatcher {
    private static final long serialVersionUID = 1L;


    @Override public boolean matches(PPath path) {
        return Files.isDirectory(path.getJavaPath());
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
        return "DirectoryPathMatcher";
    }
}
