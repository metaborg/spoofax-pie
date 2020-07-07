package mb.common.result;

import mb.common.message.KeyedMessages;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class KeyedMessagesException extends Exception {
    private final KeyedMessages messages;


    public KeyedMessagesException(KeyedMessages messages, @Nullable String description, @Nullable Throwable cause, boolean createStackTrace, boolean enableSuppression) {
        super(description, cause, enableSuppression, createStackTrace);
        this.messages = messages;
    }

    public KeyedMessagesException(KeyedMessages messages, @Nullable String description, @Nullable Throwable cause, boolean createStackTrace) {
        this(messages, description, cause, createStackTrace, true);
    }

    public KeyedMessagesException(KeyedMessages messages, @Nullable String description, @Nullable Throwable cause) {
        this(messages, description, cause, false /* By default, no stacktrace*/);
    }

    public KeyedMessagesException(KeyedMessages messages, @Nullable String description) {
        this(messages, description, null);
    }

    public KeyedMessagesException(KeyedMessages messages) {
        this(messages, null);
    }

    public KeyedMessagesException(@Nullable String description, @Nullable Throwable cause) {
        this(KeyedMessages.of(), description, cause);
    }

    public KeyedMessagesException(@Nullable String description) {
        this(KeyedMessages.of(), description);
    }

    public KeyedMessagesException() {
        this(KeyedMessages.of());
    }


    public static KeyedMessagesException withStackTrace(KeyedMessages messages) {
        return new KeyedMessagesException(messages);
    }

    public static KeyedMessagesException withStackTrace(KeyedMessages messages, @Nullable String description) {
        return new KeyedMessagesException(messages, description);
    }

    public static KeyedMessagesException withStackTrace(KeyedMessages messages, @Nullable String description, @Nullable Throwable cause) {
        return new KeyedMessagesException(messages, description, cause);
    }


    public KeyedMessages getMessages() {
        return messages;
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final KeyedMessagesException that = (KeyedMessagesException)o;
        return messages.equals(that.messages);
    }

    @Override public int hashCode() {
        return Objects.hash(messages);
    }

    @Override public String toString() {
        return "MessagesException(" + messages + ")";
    }
}
