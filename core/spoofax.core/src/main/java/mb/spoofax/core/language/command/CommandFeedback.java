package mb.spoofax.core.language.command;

import mb.common.message.KeyedMessages;
import mb.common.message.Message;
import mb.common.message.Messages;
import mb.common.region.Region;
import mb.common.result.KeyedMessagesError;
import mb.common.result.MessagesError;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.cli.CliParams;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class CommandFeedback implements Serializable {
    public interface Cases<R> {
        R showFile(ResourceKey file, @Nullable Region region);

        R showText(String text, String name, @Nullable Region region);

        R messagesError(MessagesError messagesError, @Nullable ResourceKey resource);

        R keyedMessagesError(KeyedMessagesError keyedMessagesError, @Nullable ResourceKey resource);

        R exceptionError(Exception exception);
    }

    public static CommandFeedback showFile(ResourceKey file, @Nullable Region region) {
        return CommandFeedbacks.showFile(file, region);
    }

    public static CommandFeedback showFile(ResourceKey file) {
        return CommandFeedbacks.showFile(file, null);
    }

    public static CommandFeedback showText(String text, String name, @Nullable Region region) {
        return CommandFeedbacks.showText(text, name, region);
    }

    public static CommandFeedback showText(String text, String name) {
        return CommandFeedbacks.showText(text, name, null);
    }

    public static CommandFeedback fromException(Exception exception, @Nullable ResourceKey resource) {
        if(exception instanceof MessagesError) {
            return CommandFeedbacks.messagesError((MessagesError)exception, resource);
        } else if(exception instanceof KeyedMessagesError) {
            return CommandFeedbacks.keyedMessagesError((KeyedMessagesError)exception, resource);
        } else {
            return CommandFeedbacks.exceptionError(exception);
        }
    }


    public abstract <R> R match(Cases<R> cases);

    public CommandFeedbacks.CaseOfMatchers.TotalMatcher_ShowFile caseOf() {
        return CommandFeedbacks.caseOf(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
