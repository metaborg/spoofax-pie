package mb.statix.multilang;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Message;
import mb.common.message.Messages;
import mb.common.message.Severity;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MultiLangAnalysisException extends Exception {

    private @Nullable ResourceKey resourceKey = null;

    public MultiLangAnalysisException(String s) {
        super(s);
    }

    public MultiLangAnalysisException(Throwable throwable) {
        super(throwable);
    }

    public MultiLangAnalysisException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public MultiLangAnalysisException(ResourceKey resourceKey, String s, Throwable throwable) {
        super(s, throwable);
        this.resourceKey = resourceKey;
    }

    public KeyedMessages toKeyedMessages() {
        KeyedMessagesBuilder builder = new KeyedMessagesBuilder();

        if(resourceKey == null) {
            builder.addMessage(this.toMessage());
        } else {
            builder.addMessage(this.toMessage(), resourceKey);
        }

        if(getCause() != null) {
            builder.addMessages(throwableToKeyedMessages(getCause()));
        }

        for(Throwable throwable : getSuppressed()) {
            builder.addMessages(throwableToKeyedMessages(throwable));
        }

        return builder.build();
    }

    protected Message toMessage() {
        return new Message(this.getMessage(), this, Severity.Error);
    }

    protected KeyedMessages throwableToKeyedMessages(Throwable throwable) {
        if(throwable instanceof MultiLangAnalysisException) {
            return ((MultiLangAnalysisException)throwable).toKeyedMessages();
        }
        return KeyedMessages.of(Messages.of(new Message(throwable.getMessage(), throwable, Severity.Error)));
    }

    public static MultiLangAnalysisException wrapIfNeeded(Throwable throwable) {
        if(throwable instanceof MultiLangAnalysisException) {
            return (MultiLangAnalysisException) throwable;
        }
        return new MultiLangAnalysisException(throwable);
    }

    public static MultiLangAnalysisException wrapIfNeeded(String message, Throwable throwable) {
        if(throwable instanceof MultiLangAnalysisException) {
            return (MultiLangAnalysisException) throwable;
        }
        return new MultiLangAnalysisException(message, throwable);
    }
}
