package mb.common.message;

import mb.common.util.MultiHashMap;

import java.util.ArrayList;
import java.util.Collection;

public class KeyedMessages<K> {
    final MultiHashMap<K, Message> messages;


    public KeyedMessages(MultiHashMap<K, Message> messages) {
        this.messages = messages;
    }

    public KeyedMessages() {
        this.messages = new MultiHashMap<>();
    }


    public ArrayList<Message> messages(K key) {
        return messages.get(key);
    }

    public MultiHashMap<K, Message> messages() {
        return messages;
    }


    public boolean containsSeverity(MessageSeverity severity) {
        for(Collection<Message> messages : messages.values()) {
            if(MessageUtil.containsSeverity(messages, severity)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsError() {
        return containsSeverity(MessageSeverity.Error);
    }

    public boolean containsWarning() {
        return containsSeverity(MessageSeverity.Warn);
    }

    public boolean containsInfo() {
        return containsSeverity(MessageSeverity.Info);
    }

    public boolean containsDebug() {
        return containsSeverity(MessageSeverity.Debug);
    }

    public boolean containsTrace() {
        return containsSeverity(MessageSeverity.Trace);
    }


    public boolean containsSeverityOrHigher(MessageSeverity severity) {
        for(Collection<Message> messages : messages.values()) {
            if(MessageUtil.containsSeverityOrHigher(messages, severity)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsErrorOrHigher() {
        return containsSeverityOrHigher(MessageSeverity.Error);
    }

    public boolean containsWarningOrHigher() {
        return containsSeverityOrHigher(MessageSeverity.Warn);
    }

    public boolean containsInfoOrHigher() {
        return containsSeverityOrHigher(MessageSeverity.Info);
    }

    public boolean containsDebugOrHigher() {
        return containsSeverityOrHigher(MessageSeverity.Debug);
    }

    public boolean containsTraceOrHigher() {
        return containsSeverityOrHigher(MessageSeverity.Trace);
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final KeyedMessages that = (KeyedMessages) o;
        return messages.equals(that.messages);
    }

    @Override public int hashCode() {
        return messages.hashCode();
    }

    @Override public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        messages.forEach((c, ms) -> {
            stringBuilder.append(c);
            stringBuilder.append(":\n");
            ms.forEach(m -> {
                stringBuilder.append("  ");
                stringBuilder.append(m.toString());
                stringBuilder.append('\n');
            });
        });
        return stringBuilder.toString();
    }
}
