package mb.common.message;

import java.io.Serializable;
import java.util.ArrayList;

public class MessageCollection implements Serializable {
    private final ArrayList<Message> messages;


    public MessageCollection(ArrayList<Message> messages) {
        this.messages = messages;
    }


    public Iterable<Message> getMessages() {
        return messages;
    }

    public int getSize() {
        return messages.size();
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }


    public boolean containsSeverity(MessageSeverity severity) {
        if(MessageUtil.containsSeverity(messages, severity)) {
            return true;
        }
        return false;
    }

    public boolean containsError() {
        return containsSeverity(MessageSeverity.Error);
    }

    public boolean containsWarning() {
        return containsSeverity(MessageSeverity.Warn);
    }


    public boolean containsSeverityOrHigher(MessageSeverity severity) {
        if(MessageUtil.containsSeverityOrHigher(messages, severity)) {
            return true;
        }
        return false;
    }

    public boolean containsWarningOrHigher() {
        return containsSeverityOrHigher(MessageSeverity.Warn);
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final MessageCollection that = (MessageCollection) o;
        return messages.equals(that.messages);
    }

    @Override public int hashCode() {
        return messages.hashCode();
    }

    @Override public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        messages.forEach(m -> {
            stringBuilder.append(m.toString());
            stringBuilder.append('\n');
        });
        return stringBuilder.toString();
    }
}
