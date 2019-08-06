package mb.spoofax.core.language.command.arg;

import org.checkerframework.checker.nullness.qual.Nullable;

public interface RawArgs {
    @Nullable <T> T getOption(String name);

    @Nullable <T> T getPositional(int index);
}
