package mb.spoofax.core.language.transform;

import mb.common.message.Messages;

import java.util.Objects;

public class MessagesFeedback implements TransformFeedback {
    private final Messages messages;

    public MessagesFeedback(Messages messages) {
        this.messages = messages;
    }

    public Messages getMessages() {
        return messages;
    }

    @Override public void accept(TransformFeedbackVisitor visitor) {
        visitor.messages(messages);
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final MessagesFeedback that = (MessagesFeedback) o;
        return messages.equals(that.messages);
    }

    @Override public int hashCode() {
        return Objects.hash(messages);
    }

    @Override public String toString() {
        return messages.toString();
    }
}
