package mb.spoofax.core.language.command;

import mb.spoofax.core.language.command.arg.RawArgs;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class AutoCommandRequest<A extends Serializable> {
    public final CommandDef<? extends A> def;
    public final @Nullable RawArgs initialArgs;

    public AutoCommandRequest(CommandDef<? extends A> def, @Nullable RawArgs initialArgs) {
        this.def = def;
        this.initialArgs = initialArgs;
    }

    public AutoCommandRequest(CommandDef<? extends A> def) {
        this(def, null);
    }

    public CommandRequest<? extends A> toCommandRequest() {
        return new CommandRequest<>(this.def, CommandExecutionType.AutomaticContinuous, initialArgs);
    }
}
