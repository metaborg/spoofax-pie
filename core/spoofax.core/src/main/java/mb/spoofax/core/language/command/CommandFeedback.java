package mb.spoofax.core.language.command;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.util.ListView;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class CommandFeedback implements Serializable {
    private final KeyedMessages messages;
    private final @Nullable Throwable exception;
    private final ListView<ShowFeedback> showFeedbacks;


    public CommandFeedback(KeyedMessages messages, @Nullable Throwable exception, ListView<ShowFeedback> showFeedbacks) {
        this.showFeedbacks = showFeedbacks;
        this.messages = messages;
        this.exception = exception;
    }


    public static CommandFeedback of() {
        return new CommandFeedback(KeyedMessages.of(), null, ListView.of());
    }

    public static CommandFeedback of(KeyedMessages keyedMessages) {
        return new CommandFeedback(keyedMessages, null, ListView.of());
    }

    public static CommandFeedback of(Throwable exception) {
        return new CommandFeedback(KeyedMessages.of(), exception, ListView.of());
    }

    public static CommandFeedback of(ShowFeedback showFeedback) {
        return new CommandFeedback(KeyedMessages.of(), null, ListView.of(showFeedback));
    }

    public static CommandFeedback of(ShowFeedback... showFeedbacks) {
        return new CommandFeedback(KeyedMessages.of(), null, ListView.of(showFeedbacks));
    }

    public static CommandFeedback of(ListView<ShowFeedback> showFeedbacks) {
        return new CommandFeedback(KeyedMessages.of(), null, showFeedbacks);
    }

    public static CommandFeedback of(KeyedMessages keyedMessages, ShowFeedback showFeedback) {
        return new CommandFeedback(keyedMessages, null, ListView.of(showFeedback));
    }

    public static CommandFeedback of(KeyedMessages keyedMessages, ShowFeedback... showFeedbacks) {
        return new CommandFeedback(keyedMessages, null, ListView.of(showFeedbacks));
    }

    public static CommandFeedback of(KeyedMessages keyedMessages, ListView<ShowFeedback> showFeedbacks) {
        return new CommandFeedback(keyedMessages, null, showFeedbacks);
    }

    public static CommandFeedback ofTryExtractMessagesFrom(Throwable throwable, @Nullable ResourceKey resourceForMessagesWithoutKeys) {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        if(messagesBuilder.optionalExtractMessagesRecursivelyWithFallbackKey(throwable, resourceForMessagesWithoutKeys)) {
            return new CommandFeedback(messagesBuilder.build(), null, ListView.of());
        } else {
            return new CommandFeedback(KeyedMessages.of(), throwable, ListView.of());
        }
    }

    public static CommandFeedback ofTryExtractMessagesFrom(Throwable throwable) {
        return ofTryExtractMessagesFrom(throwable, null);
    }


    public ListView<ShowFeedback> getShowFeedbacks() {
        return showFeedbacks;
    }

    public KeyedMessages getMessages() {
        return messages;
    }

    public @Nullable Throwable getException() {
        return exception;
    }


    public boolean hasErrorMessages() {
        return messages.containsErrorOrHigher();
    }

    public boolean hasException() {
        return exception != null;
    }

    public boolean hasErrorMessagesOrException() {
        return hasErrorMessages() || hasException();
    }


    @Override public String toString() {
        return "CommandFeedback{" +
            "messages=" + messages +
            ", exception=" + exception +
            ", showFeedbacks=" + showFeedbacks +
            '}';
    }
}
