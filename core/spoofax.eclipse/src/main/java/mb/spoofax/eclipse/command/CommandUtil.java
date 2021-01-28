package mb.spoofax.eclipse.command;

import mb.common.util.ListView;
import mb.spoofax.core.language.command.*;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandUtil {
    public static ListView<CommandContext> context(CommandContext context) {
        return ListView.of(context);
    }

    public static ListView<CommandContext> contexts(Stream<CommandContext> contexts) {
        return new ListView<>(contexts.collect(Collectors.toList()));
    }
}
