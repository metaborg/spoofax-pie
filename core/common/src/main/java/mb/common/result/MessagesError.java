package mb.common.result;

import mb.common.message.Messages;
import mb.common.util.ListView;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class MessagesError extends DefaultError {
    private final Messages messages;


    public MessagesError(String description, Messages messages, @Nullable Error cause, @Nullable ListView<StackTraceElement> stackTrace) {
        super(description, cause, stackTrace);
        this.messages = messages;
    }

    public MessagesError(String description, Messages messages, @Nullable Error cause) {
        this(description, messages, cause, null);
    }

    public MessagesError(String description, Messages messages, @Nullable Error cause, boolean createStackTrace) {
        this(description, messages, cause, DefaultError.createStackTrace(createStackTrace));
    }

    public MessagesError(String description, Messages messages) {
        this(description, messages, null, null);
    }

    public MessagesError(String description, Messages messages, boolean createStackTrace) {
        this(description, messages, null, DefaultError.createStackTrace(createStackTrace));
    }

    public MessagesError(String description, @Nullable Error cause) {
        this(description, Messages.of(), cause, null);
    }

    public MessagesError(String description, @Nullable Error cause, boolean createStackTrace) {
        this(description, Messages.of(), cause, DefaultError.createStackTrace(createStackTrace));
    }

    public MessagesError(String description) {
        this(description, Messages.of(), null, null);
    }

    public MessagesError(String description, boolean createStackTrace) {
        this(description, Messages.of(), null, DefaultError.createStackTrace(createStackTrace));
    }


    public Messages getMessages() {
        return messages;
    }

    // TODO: put messages in the description


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;
        final MessagesError that = (MessagesError)o;
        return messages.equals(that.messages);
    }

    @Override public int hashCode() {
        return Objects.hash(super.hashCode(), messages);
    }

    @Override public String toString() {
        return "MessagesError{" +
            "messages=" + messages +
            '}';
    }
}
