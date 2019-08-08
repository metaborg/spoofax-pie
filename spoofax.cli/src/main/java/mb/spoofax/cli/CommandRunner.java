package mb.spoofax.cli;

import mb.pie.api.PieSession;
import mb.pie.api.Task;
import mb.spoofax.core.language.command.*;
import mb.spoofax.core.language.command.arg.DefaultArgConverters;
import mb.spoofax.core.language.command.arg.RawArgs;
import mb.spoofax.core.language.command.arg.RawArgsBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.Callable;

class CommandRunner<A extends Serializable> implements Callable {
    private final PieSession pieSession;
    private final CommandDef<A> commandDef;
    private final RawArgsBuilder rawArgsBuilder;

    CommandRunner(PieSession pieSession, CommandDef<A> commandDef, DefaultArgConverters defaultArgConverters) {
        this.pieSession = pieSession;
        this.commandDef = commandDef;
        this.rawArgsBuilder = new RawArgsBuilder(commandDef.getParamDef(), defaultArgConverters);
    }

    void set(String paramId, @Nullable Object value) throws IllegalArgumentException {
        if(value == null) {
            return;
        }
        if(!(value instanceof Serializable)) {
            throw new IllegalArgumentException("Cannot set argument '" + value + "', it does not implement Serializable");
        }
        rawArgsBuilder.setArg(paramId, (Serializable) value);
    }

    @Override public @Nullable Object call() throws Exception {
        final RawArgs rawArgs = rawArgsBuilder.build(CommandContexts.none());
        final A args = commandDef.fromRawArgs(rawArgs);
        final Task<CommandOutput> task = commandDef.createTask(new CommandInput<>(args));
        final CommandOutput output = pieSession.requireWithoutObserving(task);
        for(CommandFeedback feedback : output.feedback) {
            CommandFeedbacks.caseOf(feedback)
                .openEditorForFile((file, region) -> {
                    System.out.println(file);
                    return Optional.empty();
                })
                .openEditorWithText((text, name, region) -> {
                    System.out.println(text);
                    return Optional.empty();
                });
        }
        return null;
    }
}
