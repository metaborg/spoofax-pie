package mb.statix.multilang;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Message;
import mb.common.message.Messages;
import mb.common.message.Severity;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class MultiLangAnalysisException extends Exception implements Serializable {

    private @Nullable ResourceKey resourceKey = null;
    // If true, toKeyedMessages will include a message for this exception
    // By default, it is true when no cause is provided, but false otherwise
    private final boolean includeMessage;

    public MultiLangAnalysisException(String s) {
        this(s, true);
    }

    public MultiLangAnalysisException(String s, boolean includeMessage) {
        super(s);
        this.includeMessage = includeMessage;
    }

    public MultiLangAnalysisException(Throwable throwable) {
        this(throwable, false);
    }

    public MultiLangAnalysisException(Throwable throwable, boolean includeMessage) {
        super(throwable);
        this.includeMessage = includeMessage;
    }

    public MultiLangAnalysisException(String message, Throwable throwable) {
        this(message, throwable, false);
    }

    public MultiLangAnalysisException(String message, Throwable throwable, boolean includeMessage) {
        super(message, throwable);
        this.includeMessage = includeMessage;
    }

    public MultiLangAnalysisException(ResourceKey resourceKey, String message, Throwable throwable) {
        this(resourceKey, message, throwable, false);
    }

    public MultiLangAnalysisException(ResourceKey resourceKey, String message, Throwable throwable, boolean includeMessage) {
        super(message, throwable);
        this.resourceKey = resourceKey;
        this.includeMessage = includeMessage;
    }

    public KeyedMessages toKeyedMessages() {
        return toKeyedMessages(true);
    }

    protected KeyedMessages toKeyedMessages(boolean root) {
        KeyedMessagesBuilder builder = new KeyedMessagesBuilder();

        if(getCause() != null) {
            builder.addMessages(throwableToKeyedMessages(getCause()));
        }

        for(Throwable throwable : getSuppressed()) {
            builder.addMessages(throwableToKeyedMessages(throwable));
        }

        // Add message for this exception when it is needed, or when there are no other messages (which may make this exception unnoticed)
        if((root && builder.isEmpty()) || includeMessage) {
            if(resourceKey == null) {
                builder.addMessage(this.toMessage());
            } else {
                builder.addMessage(this.toMessage(), resourceKey);
            }
        }

        return builder.build();
    }

    protected Message toMessage() {
        return new Message(this.getMessage(), this, Severity.Error);
    }

    protected KeyedMessages throwableToKeyedMessages(Throwable throwable) {
        if(throwable instanceof MultiLangAnalysisException) {
            return ((MultiLangAnalysisException)throwable).toKeyedMessages(false);
        }
        final Message message = new Message(throwable.getMessage(), throwable, Severity.Error);
        if(this.resourceKey != null) {
            return KeyedMessages.of(resourceKey, new ArrayList<>(Collections.singleton(message)));
        }
        return KeyedMessages.of(Messages.of(message));
    }

    public static MultiLangAnalysisException wrapIfNeeded(Throwable throwable) {
        if(throwable instanceof MultiLangAnalysisException) {
            return (MultiLangAnalysisException)throwable;
        }
        return new MultiLangAnalysisException(throwable);
    }

    public static MultiLangAnalysisException wrapIfNeeded(String message, Throwable throwable) {
        if(throwable instanceof MultiLangAnalysisException) {
            return (MultiLangAnalysisException)throwable;
        }
        return new MultiLangAnalysisException(message, throwable);
    }
}
