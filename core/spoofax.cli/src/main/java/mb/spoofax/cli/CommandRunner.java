package mb.spoofax.cli;

import mb.common.message.KeyedMessages;
import mb.pie.api.MixedSession;
import mb.pie.api.Task;
import mb.resource.ReadableResource;
import mb.resource.ResourceRuntimeException;
import mb.resource.ResourceService;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.spoofax.core.language.command.arg.ArgConverters;
import mb.spoofax.core.language.command.arg.RawArgs;
import mb.spoofax.core.language.command.arg.RawArgsBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.Callable;

class CommandRunner<A extends Serializable> implements Callable {
    private final ResourceService resourceService;
    private final MixedSession session;
    private final CommandDef<A> commandDef;
    private final RawArgsBuilder rawArgsBuilder;

    CommandRunner(ResourceService resourceService, MixedSession session, CommandDef<A> commandDef, ArgConverters argConverters) {
        this.resourceService = resourceService;
        this.session = session;
        this.commandDef = commandDef;
        this.rawArgsBuilder = new RawArgsBuilder(commandDef.getParamDef(), argConverters);
    }

    void set(String paramId, @Nullable Object value) throws IllegalArgumentException {
        if(value == null) {
            return;
        }
        if(!(value instanceof Serializable)) {
            throw new IllegalArgumentException("Cannot set argument '" + value + "', it does not implement Serializable");
        }
        rawArgsBuilder.setArg(paramId, (Serializable)value);
    }

    @Override public @Nullable Object call() throws Exception {
        final RawArgs rawArgs = rawArgsBuilder.build(new CommandContext());
        final A args = commandDef.fromRawArgs(rawArgs);
        final Task<CommandFeedback> task = commandDef.createTask(args);
        final CommandFeedback feedback = session.requireWithoutObserving(task);

        final @Nullable Throwable exception = feedback.getException();
        if(exception != null) {
            System.err.println("An exception occurred while executing command '" + commandDef.getDisplayName() + "':");
            exception.printStackTrace(System.err);
        }

        final KeyedMessages keyedMessages = feedback.getMessages();
        if(!keyedMessages.isEmpty()) {
            System.out.println("The following messages were produced by command '" + commandDef.getDisplayName() + "':\n" + keyedMessages.toString());
        }

        for(ShowFeedback showFeedback : feedback.getShowFeedbacks()) {
            showFeedback.caseOf()
                .showFile((file, region) -> {
                    try {
                        final ReadableResource resource = resourceService.getReadableResource(file);
                        final String text = resource.readString();
                        System.out.println(file + ":");
                        System.out.println();
                        System.out.println(text);
                    } catch(IOException | ResourceRuntimeException e) {
                        System.err.println("An exception occurred while showing file '" + file + "':");
                        e.printStackTrace(System.err);
                    }
                    return Optional.empty();
                })
                .showText((text, name, region) -> {
                    System.out.println(name + ":");
                    System.out.println();
                    System.out.println(text);
                    return Optional.empty();
                });
        }
        return null;
    }
}
