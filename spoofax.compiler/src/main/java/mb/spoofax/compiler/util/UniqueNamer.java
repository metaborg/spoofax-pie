package mb.spoofax.compiler.util;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;

public class UniqueNamer {
    private final HashMap<String, Integer> names = new HashMap<>();

    public String makeUnique(String name) {
        final @Nullable Integer number = names.get(name);
        if(number == null) {
            names.put(name, 1);
            return name;
        }
        names.put(name, number + 1);
        return name + number;
    }
}
