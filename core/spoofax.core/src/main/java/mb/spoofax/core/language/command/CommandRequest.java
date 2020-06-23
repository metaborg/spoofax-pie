package mb.spoofax.core.language.command;

import mb.pie.api.Task;
import mb.spoofax.core.language.command.arg.ArgConverters;
import mb.spoofax.core.language.command.arg.RawArgs;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Optional;

@Value.Immutable
public interface CommandRequest<A extends Serializable> {
    class Builder<A extends Serializable> extends ImmutableCommandRequest.Builder<A> {}

    static Builder builder() { return new Builder(); }

    static <A extends Serializable> CommandRequest<A> of(CommandDef<A> def, CommandExecutionType executionType, @Nullable RawArgs initialArgs) {
        return ImmutableCommandRequest.of(def, executionType, Optional.ofNullable(initialArgs));
    }

    static <A extends Serializable> CommandRequest<A> of(CommandDef<A> def, CommandExecutionType executionType) {
        return ImmutableCommandRequest.of(def, executionType, Optional.empty());
    }


    @Value.Parameter CommandDef<A> def();

    @Value.Parameter CommandExecutionType executionType();

    @Value.Parameter Optional<RawArgs> initialArgs();


    default Task<CommandFeedback> createTask(CommandContext context, ArgConverters argConverters) {
        return def().createTask(this, context, argConverters);
    }
}
