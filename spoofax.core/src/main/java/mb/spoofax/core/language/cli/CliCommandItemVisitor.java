package mb.spoofax.core.language.cli;

import mb.spoofax.core.language.command.CommandDef;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface CliCommandItemVisitor {
    void commandListPush(String name, @Nullable String description);

    void commandListPop();

    void command(CommandDef<?> def, String name, CliParamDef cliParamDef, @Nullable String description);
}
