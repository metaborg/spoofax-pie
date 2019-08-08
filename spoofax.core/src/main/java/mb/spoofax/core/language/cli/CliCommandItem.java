package mb.spoofax.core.language.cli;

import org.checkerframework.checker.nullness.qual.Nullable;

public interface CliCommandItem {
    String getName();

    @Nullable String getDescription();


    void accept(CliCommandItemVisitor visitor);
}
