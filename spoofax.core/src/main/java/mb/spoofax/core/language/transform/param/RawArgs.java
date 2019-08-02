package mb.spoofax.core.language.transform.param;

import org.checkerframework.checker.nullness.qual.Nullable;

public interface RawArgs {
    @Nullable <T> T getOption(String name);

    @Nullable <T> T getPositional(int index);
}
