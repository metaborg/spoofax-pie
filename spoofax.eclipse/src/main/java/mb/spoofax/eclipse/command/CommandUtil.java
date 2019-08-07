package mb.spoofax.eclipse.command;

import mb.common.util.ListView;
import mb.pie.api.Task;
import mb.spoofax.core.language.command.*;
import mb.spoofax.core.language.command.arg.RawArgs;
import mb.spoofax.core.language.command.arg.RawArgsBuilder;

import java.io.Serializable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandUtil {
    public static ListView<CommandContext> context(CommandContext context) {
        return ListView.of(context);
    }

    public static ListView<CommandContext> contexts(Stream<CommandContext> contexts) {
        return new ListView<>(contexts.collect(Collectors.toList()));
    }

    public static <A extends Serializable> Task<CommandOutput> createTask(CommandRequest<A> commandRequest, CommandContext context) {
        final CommandDef<A> def = commandRequest.def;
        final RawArgsBuilder builder = new RawArgsBuilder(def.getParamDef());
        if(commandRequest.initialArgs != null) {
            builder.setAndAddArgsFrom(commandRequest.initialArgs);
        }
        final RawArgs rawArgs = builder.build(context);
        final A args = def.fromRawArgs(rawArgs);
        final CommandInput<A> input = new CommandInput<>(args);
        return def.createTask(input);
    }
}
