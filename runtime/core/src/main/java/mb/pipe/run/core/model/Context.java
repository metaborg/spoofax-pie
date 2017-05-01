package mb.pipe.run.core.model;

import mb.pipe.run.core.vfs.IResource;

public class Context implements IContext {
    private static final long serialVersionUID = 1L;

    private final IResource currentDir;
    private final IResource persistentDir;


    public Context(IResource currentDir, IResource persistentDir) {
        this.currentDir = currentDir;
        this.persistentDir = persistentDir;
    }

    public Context(IResource currentDir) {
        this(currentDir, currentDir.resolve("target/pluto"));
    }


    @Override public IResource currentDir() {
        return currentDir;
    }

    @Override public IResource persistentDir() {
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
        final Context other = (Context) obj;
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
