package mb.common.message;

import mb.common.util.ListView;
import mb.common.util.MultiHashMap;
import mb.resource.ResourceKey;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class Messages implements Iterable<Message>, Serializable {
    final ListView<Message> messages;


    public Messages(ListView<Message> messages) {
        this.messages = messages;
    }

    public static Messages of() {
        return new Messages(ListView.of());
    }

    public static Messages of(Message message) {
        return new Messages(ListView.of(message));
    }

    public static Messages of(Message... messages) {
        return new Messages(ListView.of(messages));
    }

    public static Messages of(List<? extends Message> list) {
        return new Messages(ListView.of(list));
    }

    public static Messages copyOf(Iterable<Message> messages) {
        return new Messages(ListView.copyOf(messages));
    }

    public static Messages copyOf(Collection<Message> messages) {
        return new Messages(ListView.copyOf(messages));
    }


    public int size() {
        return messages.size();
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public boolean containsSeverity(Severity severity) {
        return messages.stream().anyMatch(
            message -> message.severity.equals(severity)
        );
    }

    public boolean containsError() {
        return containsSeverity(Severity.Error);
    }

    public boolean containsWarning() {
        return containsSeverity(Severity.Warning);
    }

    public boolean containsInfo() {
        return containsSeverity(Severity.Info);
    }

    public boolean containsDebug() {
        return containsSeverity(Severity.Debug);
    }

    public boolean containsTrace() {
        return containsSeverity(Severity.Trace);
    }


    public boolean containsSeverityOrHigher(Severity severity) {
        return messages.stream().anyMatch(
            message -> message.severity.compareTo(severity) >= 0
        );
    }

    public boolean containsErrorOrHigher() {
        return containsSeverityOrHigher(Severity.Error);
    }

    public boolean containsWarningOrHigher() {
        return containsSeverityOrHigher(Severity.Warning);
    }

    public boolean containsInfoOrHigher() {
        return containsSeverityOrHigher(Severity.Info);
    }

    public boolean containsDebugOrHigher() {
        return containsSeverityOrHigher(Severity.Debug);
    }

    public boolean containsTraceOrHigher() {
        return containsSeverityOrHigher(Severity.Trace);
    }


    public KeyedMessages toKeyed(ResourceKey key) {
        final MultiHashMap<ResourceKey, Message> map = new MultiHashMap<>();
        map.putAll(key, messages);
        return KeyedMessages.copyOf(map.getInnerMap());
    }

    public KeyedMessages toKeyed() {
        return KeyedMessages.of(messages);
    }


    @Override public Iterator<Message> iterator() {
        return messages.iterator();
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Messages that = (Messages)o;
        return messages.equals(that.messages);
    }

    @Override public int hashCode() {
        return messages.hashCode();
    }

    @Override public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        messages.forEach((m) -> {
            stringBuilder.append(m.toString());
            stringBuilder.append('\n');
        });
        return stringBuilder.toString();
    }
}
