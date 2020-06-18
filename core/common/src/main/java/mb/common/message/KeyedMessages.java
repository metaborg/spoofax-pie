package mb.common.message;

import mb.common.util.ListView;
import mb.common.util.MapView;
import mb.common.util.SetView;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class KeyedMessages implements Serializable {
    final MapView<@Nullable ResourceKey, ArrayList<Message>> messages;


    public KeyedMessages(MapView<@Nullable ResourceKey, ArrayList<Message>> messages) {
        this.messages = messages;
    }

    public static KeyedMessages of() {
        return new KeyedMessages(MapView.of());
    }

    public static KeyedMessages of(@Nullable ResourceKey resource, ArrayList<Message> messages) {
        return new KeyedMessages(MapView.of(resource, messages));
    }

    public static KeyedMessages copyOf(Map<@Nullable ResourceKey, ArrayList<Message>> keyedMessages) {
        return new KeyedMessages(MapView.copyOf(keyedMessages));
    }


    public int size() {
        return messages.size();
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public Iterable<Message> getMessages(ResourceKey resource) {
        return messages.getOrDefault(resource, new ArrayList<>());
    }

    public Iterable<Message> getMessagesWithoutOrigin() {
        return messages.getOrDefault(null, new ArrayList<>());
    }

    public SetView<@Nullable ResourceKey> getResources() {
        return messages.keySet();
    }

    public MapView<ResourceKey, ArrayList<Message>> getAllMessages() {
        return messages;
    }


    public boolean containsSeverity(Severity severity) {
        return messages.values().stream().flatMap(Collection::stream).anyMatch(
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
        return messages.values().stream().flatMap(Collection::stream).anyMatch(
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


    public Messages asMessages() {
        final ArrayList<Message> list = messages.values().stream().flatMap(Collection::stream).collect(Collectors.toCollection(ArrayList::new));
        return new Messages(new ListView<>(list));
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final KeyedMessages that = (KeyedMessages)o;
        return messages.equals(that.messages);
    }

    @Override public int hashCode() {
        return messages.hashCode();
    }

    @Override public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        messages.forEach((c, ms) -> {
            if(c != null) {
                stringBuilder.append(c);
                stringBuilder.append(":\n");
                ms.forEach(m -> {
                    stringBuilder.append("  ");
                    stringBuilder.append(m.toString());
                    stringBuilder.append('\n');
                });
            } else {
                ms.forEach(m -> {
                    stringBuilder.append(m.toString());
                    stringBuilder.append('\n');
                });
            }
        });
        return stringBuilder.toString();
    }

    public int count() {
        return messages.stream()
            .map(Map.Entry::getValue)
            .mapToInt(ArrayList::size)
            .sum();
    }
}
