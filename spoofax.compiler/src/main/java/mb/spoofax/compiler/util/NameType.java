package mb.spoofax.compiler.util;

import java.io.Serializable;
import java.util.Objects;

public class NameType implements Serializable {
    public final String name;
    public final String type;

    public NameType(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final NameType nameType = (NameType)o;
        return name.equals(nameType.name) && type.equals(nameType.type);
    }

    @Override public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override public String toString() {
        return name + " : " + type;
    }
}
