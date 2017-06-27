package mb.pipe.run.spoofax.cfg;

import java.io.Serializable;
import java.util.ArrayList;

import mb.pipe.run.core.path.PPath;

public class SpxCoreLangSpecConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private final PPath location;
    private final ArrayList<String> extensions;


    public SpxCoreLangSpecConfig(PPath location, ArrayList<String> extensions) {
        this.location = location;
        this.extensions = extensions;
    }

    public static SpxCoreLangSpecConfig generate(PPath location, ArrayList<String> extensions) {
        return new SpxCoreLangSpecConfig(location, extensions);
    }


    public final PPath location() {
        return location;
    }

    public final ArrayList<String> extensions() {
        return extensions;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + location.hashCode();
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
        final SpxCoreLangSpecConfig other = (SpxCoreLangSpecConfig) obj;
        if(!location.equals(other.location))
            return false;
        if(!extensions.equals(other.extensions)) {
            return false;
        }
        return true;
    }

    @Override public String toString() {
        return location.toString();
    }
}
