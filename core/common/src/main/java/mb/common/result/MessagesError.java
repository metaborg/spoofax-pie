package mb.common.result;

import mb.common.message.Messages;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class MessagesError extends Throwable {
    private final Messages messages;


    public MessagesError(Messages messages, @Nullable String description, @Nullable Throwable cause, boolean enableSuppression, boolean createStackTrace) {
        super(description, cause, enableSuppression, createStackTrace);
        this.messages = messages;
    }

    public MessagesError(Messages messages, @Nullable String description, @Nullable Throwable cause, boolean enableSuppression) {
        this(messages, description, cause, enableSuppression, true);
    }

    public MessagesError(Messages messages, @Nullable String description, @Nullable Throwable cause) {
        this(messages, description, cause, true);
    }

    public MessagesError(Messages messages, @Nullable String description) {
        this(messages, description, null);
    }

    public MessagesError(Messages messages) {
        this(messages, null);
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
