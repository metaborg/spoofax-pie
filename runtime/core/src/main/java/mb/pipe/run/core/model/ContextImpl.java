package mb.pipe.run.core.model;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import mb.vfs.path.PPath;

public class ContextImpl implements Context {
    private static final long serialVersionUID = 1L;

    private final PPath currentDir;


    @Inject public ContextImpl(@Assisted PPath currentDir) {
        this.currentDir = currentDir;
    }

    @Override public PPath currentDir() {
        return currentDir;
    }



    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + currentDir.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final ContextImpl other = (ContextImpl) obj;
        if(!currentDir.equals(other.currentDir))
            return false;
        return true;
    }

    @Override public String toString() {
        return currentDir.toString();
    }
}
