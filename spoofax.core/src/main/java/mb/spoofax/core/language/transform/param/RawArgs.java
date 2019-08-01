package mb.spoofax.core.language.transform.param;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public interface RawArgs {
    @Nullable <T extends Serializable> T getOption(String name);

    @Nullable <T extends Serializable> T getPositional(int index);
}
