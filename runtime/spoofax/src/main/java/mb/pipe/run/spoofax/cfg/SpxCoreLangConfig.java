package mb.pipe.run.spoofax.cfg;

import java.io.Serializable;

import mb.pipe.run.core.path.PPath;

public class SpxCoreLangConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private final PPath location;
    private final String extension;


    public SpxCoreLangConfig(PPath location, String extension) {
        this.location = location;
        this.extension = extension;
    }

    public static SpxCoreLangConfig generate(PPath location, String extension) {
        return new SpxCoreLangConfig(location, extension);
    }


    public final PPath location() {
        return location;
    }

    public final String extension() {
        return extension;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + extension.hashCode();
        result = prime * result + location.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final SpxCoreLangConfig other = (SpxCoreLangConfig) obj;
        if(!extension.equals(other.extension))
            return false;
        if(!location.equals(other.location))
            return false;
        return true;
    }

    @Override public String toString() {
        return extension;
    }
}
