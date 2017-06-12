package mb.pipe.run.core.model;

import mb.pipe.run.core.path.PPath;

public class ContextImpl implements Context {
    private static final long serialVersionUID = 1L;

    private final PPath currentDir;
    private final PPath persistentDir;


    public ContextImpl(PPath currentDir, PPath persistentDir) {
        this.currentDir = currentDir;
        this.persistentDir = persistentDir;
    }

    public ContextImpl(PPath currentDir) {
        this(currentDir, currentDir.resolve("target/pluto"));
    }


    @Override public PPath currentDir() {
        return currentDir;
    }

    @Override public PPath persistentDir() {
        return persistentDir;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + currentDir.hashCode();
        result = prime * result + persistentDir.hashCode();
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
        if(!persistentDir.equals(other.persistentDir))
            return false;
        return true;
    }

    @Override public String toString() {
        return currentDir.toString();
    }
}
