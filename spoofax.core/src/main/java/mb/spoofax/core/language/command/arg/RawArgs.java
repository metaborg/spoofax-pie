package mb.spoofax.core.language.command.arg;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Optional;

public interface RawArgs extends Serializable {
    <T extends Serializable> Optional<T> getOption(String name);

    default <T extends Serializable> @Nullable T getOptionOrNull(String name) {
        return this.<T>getOption(name).orElse(null);
    }

    default <T extends Serializable> T getOptionOrThrow(String name) {
        return this.<T>getOption(name).orElseThrow(() -> new RuntimeException("No option argument named '" + name + "'"));
    }

    default boolean getOptionOrFalse(String name) {
        return this.<Boolean>getOption(name).orElse(false);
    }

    default boolean getOptionOrTrue(String name) {
        return this.<Boolean>getOption(name).orElse(true);
    }


    <T extends Serializable> Optional<T> getPositional(int index);

    default <T extends Serializable> @Nullable T getPositionalOrNull(int index) {
        return this.<T>getPositional(index).orElse(null);
    }

    default <T extends Serializable> T getPositionalOrThrow(int index) {
        return this.<T>getPositional(index).orElseThrow(() -> new RuntimeException("No positional argument at index '" + index + "'"));
    }
}
