package mb.spoofax.core.language.command;

import mb.pie.api.Task;
import mb.spoofax.core.language.command.arg.ArgConverters;
import mb.spoofax.core.language.command.arg.RawArgs;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

@Value.Immutable
public interface AutoCommandRequest<A extends Serializable> {
    class Builder<A extends Serializable> extends ImmutableAutoCommandRequest.Builder<A> {}

    static <A extends Serializable> Builder<A> builder() { return new Builder<>(); }

    static <A extends Serializable> AutoCommandRequest<A> of(CommandDef<A> def, HierarchicalResourceType... resourceTypes) {
        return AutoCommandRequest.<A>builder().def(def).addResourceTypes(resourceTypes).build();
    }

    static <A extends Serializable> AutoCommandRequest<A> of(CommandDef<A> def, RawArgs initialArgs, HierarchicalResourceType... resourceTypes) {
        return AutoCommandRequest.<A>builder().def(def).initialArgs(initialArgs).addResourceTypes(resourceTypes).build();
    }


    CommandDef<A> def();

    Optional<RawArgs> initialArgs();

    Set<HierarchicalResourceType> resourceTypes();


    default CommandRequest<A> toCommandRequest() {
        return CommandRequest.of(def(), CommandExecutionType.AutomaticContinuous, initialArgs().orElse(null));
    }

    default Task<CommandFeedback> createTask(CommandContext context, ArgConverters argConverters) {
        return def().createTask(toCommandRequest(), context, argConverters);
    }
}
