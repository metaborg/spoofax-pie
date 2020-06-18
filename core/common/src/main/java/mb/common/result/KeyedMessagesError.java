package mb.common.result;

import mb.common.message.KeyedMessages;
import mb.common.message.Messages;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class KeyedMessagesError extends Exception {
    private final KeyedMessages messages;


    public KeyedMessagesError(KeyedMessages messages, @Nullable String description, @Nullable Throwable cause, boolean createStackTrace, boolean enableSuppression) {
        super(description, cause, enableSuppression, createStackTrace);
        this.messages = messages;
    }

    public KeyedMessagesError(KeyedMessages messages, @Nullable String description, @Nullable Throwable cause, boolean createStackTrace) {
        this(messages, description, cause, createStackTrace, true);
    }

    public KeyedMessagesError(KeyedMessages messages, @Nullable String description, @Nullable Throwable cause) {
        this(messages, description, cause, false /* By default, no stacktrace*/);
    }

    public KeyedMessagesError(KeyedMessages messages, @Nullable String description) {
        this(messages, description, null);
    }

    public KeyedMessagesError(KeyedMessages messages) {
        this(messages, null);
    }

    public KeyedMessagesError(@Nullable String description, @Nullable Throwable cause) {
        this(KeyedMessages.of(), description, cause);
    }

    public KeyedMessagesError(@Nullable String description) {
        this(KeyedMessages.of(), description);
    }

    public KeyedMessagesError() {
        this(KeyedMessages.of());
    }


    public static KeyedMessagesError withStackTrace(KeyedMessages messages) {
        return new KeyedMessagesError(messages);
    }

    public static KeyedMessagesError withStackTrace(KeyedMessages messages, @Nullable String description) {
        return new KeyedMessagesError(messages, description);
    }

    public static KeyedMessagesError withStackTrace(KeyedMessages messages, @Nullable String description, @Nullable Throwable cause) {
        return new KeyedMessagesError(messages, description, cause);
    }


    public KeyedMessages getMessages() {
        return messages;
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final KeyedMessagesError that = (KeyedMessagesError)o;
        return messages.equals(that.messages);
    }

    @Override public int hashCode() {
        return Objects.hash(messages);
    }

    @Override public String toString() {
        return "MessagesError(" + messages + ")";
    }
}
