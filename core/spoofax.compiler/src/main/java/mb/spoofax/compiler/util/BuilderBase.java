package mb.spoofax.compiler.util;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Properties;
import java.util.function.Consumer;

public interface BuilderBase {
    default void with(Properties properties, String id, Consumer<String> func) {
        final @Nullable String value = properties.getProperty(id);
        if(value != null) {
            func.accept(value);
        }
    }
}
