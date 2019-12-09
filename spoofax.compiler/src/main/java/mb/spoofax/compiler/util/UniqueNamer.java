package mb.spoofax.compiler.util;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UniqueNamer {
    private final HashMap<String, AtomicInteger> names = new HashMap<>();

    public void reserve(String name) {
        if(!names.containsKey(name)) {
            names.put(name, new AtomicInteger(0));
        }
    }

    public String makeUnique(String name) {
        while(true) {
            final @Nullable AtomicInteger counter = names.get(name);
            if(counter == null) {
                names.put(name, new AtomicInteger(0));
                return name;
            }
            final int count = counter.incrementAndGet();
            if(count == 0) {
                throw new IllegalStateException("Cannot create unique name from '" + name + "' counter wrapped around");
            }
            final String newName = name + count;
            if(!names.containsKey(newName)) {
                return newName;
            }
        }
    }

    public void reset() {
        names.clear();
    }
}
