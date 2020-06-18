package mb.common.result;

import mb.common.message.Messages;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class MessagesError extends Exception {
    private final Messages messages;


    public MessagesError(Messages messages, @Nullable String description, @Nullable Throwable cause, boolean createStackTrace, boolean enableSuppression) {
        super(description, cause, enableSuppression, createStackTrace);
        this.messages = messages;
    }

    public MessagesError(Messages messages, @Nullable String description, @Nullable Throwable cause, boolean createStackTrace) {
        this(messages, description, cause, createStackTrace, true);
    }

    public MessagesError(Messages messages, @Nullable String description, @Nullable Throwable cause) {
        this(messages, description, cause, false /* By default, no stacktrace*/);
    }

    public MessagesError(Messages messages, @Nullable String description) {
        this(messages, description, null);
    }

    public MessagesError(Messages messages) {
        this(messages, null);
    }

    public MessagesError(@Nullable String description, @Nullable Throwable cause) {
        this(Messages.of(), description, cause);
    }

    public MessagesError(@Nullable String description) {
        this(Messages.of(), description);
    }

    public MessagesError() {
        this(Messages.of());
    }


    public static MessagesError withStackTrace(Messages messages) {
        return new MessagesError(messages);
    }

    public static MessagesError withStackTrace(Messages messages, @Nullable String description) {
        return new MessagesError(messages, description);
    }

    public static MessagesError withStackTrace(Messages messages, @Nullable String description, @Nullable Throwable cause) {
        return new MessagesError(messages, description, cause);
    }


    public Messages getMessages() {
        return messages;
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final MessagesError that = (MessagesError)o;
        return messages.equals(that.messages);
    }

    @Override public int hashCode() {
        return Objects.hash(messages);
    }

    @Override public String toString() {
        return "MessagesError(" + messages + ")";
    }
}
