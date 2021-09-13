package mb.spoofax.compiler.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

public class TypeInfoCollection implements Iterable<TypeInfo> {
    private final ArrayList<TypeInfo> types;

    public TypeInfoCollection(Collection<TypeInfo> types) {
        this.types = new ArrayList<>(types);
    }

    public TypeInfoCollection() {
        this.types = new ArrayList<>();
    }

    public void add(TypeInfo type) {
        this.types.add(type);
    }

    public void add(TypeInfo actual, TypeInfo base) {
        this.types.add(actual);
        if(!actual.equals(base)) {
            this.types.add(base);
        }
    }

    public void addAll(Collection<TypeInfo> types) {
        this.types.addAll(types);
    }

    public Stream<TypeInfo> stream() {
        return types.stream();
    }

    @Override public Iterator<TypeInfo> iterator() {
        return types.iterator();
    }
}
