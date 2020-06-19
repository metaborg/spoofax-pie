package mb.spoofax.core.language.command;

import mb.common.message.KeyedMessages;
import mb.common.message.Messages;
import mb.common.region.Region;
import mb.common.result.KeyedMessagesError;
import mb.common.result.MessagesError;
import mb.common.util.ADT;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class CommandFeedback implements Serializable {
    public interface Cases<R> {
        R showFile(ResourceKey file, @Nullable Region region);

        R showText(String text, String name, @Nullable Region region);

        R messages(Messages messages, @Nullable ResourceKey origin);

        R keyedMessages(KeyedMessages keyedMessages, @Nullable ResourceKey defaultOrigin);

        R messagesError(MessagesError messagesError, @Nullable ResourceKey origin);

        R keyedMessagesError(KeyedMessagesError keyedMessagesError, @Nullable ResourceKey defaultOrigin);

        R exceptionError(Exception exception);
    }

    @SuppressWarnings("ConstantConditions")
    public static CommandFeedback showFile(ResourceKey file, @Nullable Region region) {
        return CommandFeedbacks.showFile(file, region);
    }

    @SuppressWarnings("ConstantConditions")
    public static CommandFeedback showFile(ResourceKey file) {
        return CommandFeedbacks.showFile(file, null);
    }

    @SuppressWarnings("ConstantConditions")
    public static CommandFeedback showText(String text, String name, @Nullable Region region) {
        return CommandFeedbacks.showText(text, name, region);
    }

    @SuppressWarnings("ConstantConditions")
    public static CommandFeedback showText(String text, String name) {
        return CommandFeedbacks.showText(text, name, null);
    }

    @SuppressWarnings("ConstantConditions")
    public static CommandFeedback messages(Messages messages, @Nullable ResourceKey origin) {
        return CommandFeedbacks.messages(messages, origin);
    }

    @SuppressWarnings("ConstantConditions")
    public static CommandFeedback messages(Messages messages) {
        return CommandFeedbacks.messages(messages, null);
    }

    @SuppressWarnings("ConstantConditions")
    public static CommandFeedback keyedMessages(KeyedMessages keyedMessages, @Nullable ResourceKey defaultOrigin) {
        return CommandFeedbacks.keyedMessages(keyedMessages, defaultOrigin);
    }

    @SuppressWarnings("ConstantConditions")
    public static CommandFeedback keyedMessages(KeyedMessages keyedMessages) {
        return CommandFeedbacks.keyedMessages(keyedMessages, null);
    }

    @SuppressWarnings("ConstantConditions")
    public static CommandFeedback messagesError(MessagesError messagesError, @Nullable ResourceKey origin) {
        return CommandFeedbacks.messagesError(messagesError, origin);
    }

    @SuppressWarnings("ConstantConditions")
    public static CommandFeedback messagesError(MessagesError messagesError) {
        return CommandFeedbacks.messagesError(messagesError, null);
    }

    @SuppressWarnings("ConstantConditions")
    public static CommandFeedback keyedMessagesError(KeyedMessagesError keyedMessagesError, @Nullable ResourceKey defaultOrigin) {
        return CommandFeedbacks.keyedMessagesError(keyedMessagesError, defaultOrigin);
    }

    @SuppressWarnings("ConstantConditions")
    public static CommandFeedback keyedMessagesError(KeyedMessagesError keyedMessagesError) {
        return CommandFeedbacks.keyedMessagesError(keyedMessagesError, null);
    }

    @SuppressWarnings("ConstantConditions")
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
