package mb.pipe.run.spoofax.cfg;

import java.io.Serializable;
import java.util.ArrayList;

import mb.pipe.run.core.path.PPath;

public class SpxCoreConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private final PPath dir;
    private final boolean isLangSpec;
    private final ArrayList<String> extensions;


    public SpxCoreConfig(PPath dir, boolean isLangSpec, ArrayList<String> extensions) {
        this.dir = dir;
        this.isLangSpec = isLangSpec;
        this.extensions = extensions;
    }

    public static SpxCoreConfig create(PPath dir, boolean isLangSpec, ArrayList<String> extensions) {
        return new SpxCoreConfig(dir, isLangSpec, extensions);
    }


    public PPath dir() {
        return dir;
    }

    public boolean isLangSpec() {
        return isLangSpec;
    }

    public ArrayList<String> extensions() {
        return extensions;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + dir.hashCode();
        result = prime * result + extensions.hashCode();
        result = prime * result + (isLangSpec ? 1231 : 1237);
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final SpxCoreConfig other = (SpxCoreConfig) obj;
        if(!dir.equals(other.dir))
            return false;
        if(!extensions.equals(other.extensions))
            return false;
        if(isLangSpec != other.isLangSpec)
            return false;
        return true;
    }

    @Override public String toString() {
        return dir.toString();
    }
}
