package mb.common.result;

import mb.common.message.Messages;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class MessagesException extends Exception {
    private final Messages messages;


    public MessagesException(Messages messages, @Nullable String description, @Nullable Throwable cause, boolean createStackTrace, boolean enableSuppression) {
        super(description, cause, enableSuppression, createStackTrace);
        this.messages = messages;
    }

    public MessagesException(Messages messages, @Nullable String description, @Nullable Throwable cause, boolean createStackTrace) {
        this(messages, description, cause, createStackTrace, true);
    }

    public MessagesException(Messages messages, @Nullable String description, @Nullable Throwable cause) {
        this(messages, description, cause, false /* By default, no stacktrace*/);
    }

    public MessagesException(Messages messages, @Nullable String description) {
        this(messages, description, null);
    }

    public MessagesException(Messages messages) {
        this(messages, null);
    }

    public MessagesException(@Nullable String description, @Nullable Throwable cause) {
        this(Messages.of(), description, cause);
    }

    public MessagesException(@Nullable String description) {
        this(Messages.of(), description);
    }

    public MessagesException() {
        this(Messages.of());
    }


    public static MessagesException withStackTrace(Messages messages) {
        return new MessagesException(messages);
    }

    public static MessagesException withStackTrace(Messages messages, @Nullable String description) {
        return new MessagesException(messages, description);
    }

    public static MessagesException withStackTrace(Messages messages, @Nullable String description, @Nullable Throwable cause) {
        return new MessagesException(messages, description, cause);
    }


    public Messages getMessages() {
        return messages;
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final MessagesException that = (MessagesException)o;
        return messages.equals(that.messages);
    }

    @Override public int hashCode() {
        return Objects.hash(messages);
    }

    @Override public String toString() {
        return "MessagesException(" + messages + ")";
    }
}
