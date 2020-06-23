package mb.spoofax.core.language.command;

import mb.common.message.KeyedMessages;
import mb.common.result.KeyedMessagesException;
import mb.common.result.MessagesException;
import mb.common.util.ListView;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class CommandFeedback implements Serializable {
    private final KeyedMessages messages;
    private final @Nullable Exception exception;
    private final ListView<ShowFeedback> showFeedbacks;


    public CommandFeedback(KeyedMessages messages, @Nullable Exception exception, ListView<ShowFeedback> showFeedbacks) {
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

    public static CommandFeedback of(Exception exception, @Nullable ResourceKey origin) {
        final KeyedMessages keyedMessages = exceptionToKeyedMessages(exception, origin);
        return new CommandFeedback(keyedMessages, exception, ListView.of());
    }

    public static CommandFeedback of(Exception exception) {
        return CommandFeedback.of(exception, null);
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


    public ListView<ShowFeedback> getShowFeedbacks() {
        return showFeedbacks;
    }

    public KeyedMessages getMessages() {
        return messages;
    }

    public @Nullable Exception getException() {
        return exception;
    }


    static KeyedMessages exceptionToKeyedMessages(Exception exception, @Nullable ResourceKey origin) {
        if(exception instanceof KeyedMessagesException) {
            final KeyedMessagesException keyedMessagesException = (KeyedMessagesException)exception;
            return keyedMessagesException.getMessages();
        } else if(exception instanceof MessagesException) {
            final MessagesException messagesException = (MessagesException)exception;
            if(origin != null) {
                return KeyedMessages.of(origin, messagesException.getMessages());
            } else {
                return KeyedMessages.of(messagesException.getMessages());
            }
        } else {
            return KeyedMessages.of();
        }
    }
}
