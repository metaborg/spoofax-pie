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
import java.util.Objects;

public class KeyedMessages implements Serializable {
    final MapView<ResourceKey, ArrayList<Message>> messages;
    final ListView<Message> messagesWithoutKey;


    public KeyedMessages(MapView<ResourceKey, ArrayList<Message>> messages, ListView<Message> messagesWithoutKey) {
        this.messages = messages;
        this.messagesWithoutKey = messagesWithoutKey;
    }

    public static KeyedMessages of() {
        return new KeyedMessages(MapView.of(), ListView.of());
    }

    public static KeyedMessages of(ResourceKey resource, ArrayList<Message> messages, ListView<Message> messagesWithoutKey) {
        return new KeyedMessages(MapView.of(resource, messages), messagesWithoutKey);
    }

    public static KeyedMessages of(ResourceKey resource, ArrayList<Message> messages) {
        return KeyedMessages.of(resource, messages, ListView.of());
    }

    public static KeyedMessages of(ListView<Message> messagesWithoutKey) {
        return new KeyedMessages(MapView.of(), messagesWithoutKey);
    }

    public static KeyedMessages of(Messages messagesWithoutKey) {
        return new KeyedMessages(MapView.of(), messagesWithoutKey.messages);
    }


    public static KeyedMessages copyOf(Map<ResourceKey, ArrayList<Message>> keyedMessages, Collection<Message> messagesWithoutKey) {
        return new KeyedMessages(MapView.copyOf(keyedMessages), ListView.copyOf(messagesWithoutKey));
    }

    public static KeyedMessages copyOf(Map<ResourceKey, ArrayList<Message>> keyedMessages) {
        return KeyedMessages.copyOf(keyedMessages, new ArrayList<>());
    }

    public static KeyedMessages copyOf(ResourceKey resource, Messages messages, ListView<Message> messagesWithoutKey) {
        return KeyedMessages.of(resource, messages.messages.asCopy(), messagesWithoutKey);
    }

    public static KeyedMessages copyOf(ResourceKey resource, Collection<? extends Message> messages, ListView<Message> messagesWithoutKey) {
        return KeyedMessages.of(resource, new ArrayList<>(messages), messagesWithoutKey);
    }

    public static KeyedMessages copyOf(ResourceKey resource, Messages messages) {
        return KeyedMessages.of(resource, messages.messages.asCopy(), ListView.of());
    }

    public static KeyedMessages copyOf(ResourceKey resource,  Collection<? extends Message> messages) {
        return KeyedMessages.of(resource, new ArrayList<>(messages), ListView.of());
    }


    public int size() {
        return messages.stream()
            .map(Map.Entry::getValue)
            .mapToInt(ArrayList::size)
            .sum()
            + messagesWithoutKey.size();
    }

    public boolean isEmpty() {
        return messages.isEmpty() && messagesWithoutKey.isEmpty();
    }

    public ListView<Message> getMessagesOfKey(ResourceKey resource) {
        final @Nullable ArrayList<Message> messagesForKey = messages.get(resource);
        if(messagesForKey == null) {
            return ListView.of();
        } else {
            return ListView.of(messagesForKey);
        }
    }

    public MapView<ResourceKey, ArrayList<Message>> getMessagesWithKey() {
        return messages;
    }

    public ListView<Message> getMessagesWithoutKey() {
        return messagesWithoutKey;
    }

    public SetView<ResourceKey> getKeys() {
        return messages.keySet();
    }


    public boolean containsSeverity(Severity severity) {
        final boolean contains = messages.values().stream().flatMap(Collection::stream).anyMatch(
            message -> message.severity.equals(severity)
        );
        if(contains) return true;
        return messagesWithoutKey.stream().anyMatch(
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
        final boolean contains = messages.values().stream().flatMap(Collection::stream).anyMatch(
            message -> message.severity.compareTo(severity) >= 0
        );
        if(contains) return true;
        return messagesWithoutKey.stream().anyMatch(
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
        final MessagesBuilder builder = new MessagesBuilder();
        for(ArrayList<Message> messages : messages.values()) {
            builder.addMessages(messages);
        }
        builder.addMessages(messagesWithoutKey);
        return builder.build();
    }


    public void addToStringBuilder(StringBuilder sb) {
        messages.forEach((c, ms) -> {
            sb.append(c);
            sb.append(":\n");
            ms.forEach(m -> {
                sb.append("  ");
                sb.append(m.toString());
                sb.append('\n');
            });
        });
        messagesWithoutKey.forEach((m) -> {
            sb.append(m.toString());
            sb.append('\n');
        });
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final KeyedMessages that = (KeyedMessages)o;
        return messages.equals(that.messages) &&
            messagesWithoutKey.equals(that.messagesWithoutKey);
    }

    @Override public int hashCode() {
        return Objects.hash(messages, messagesWithoutKey);
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();
        addToStringBuilder(sb);
        return sb.toString();
    }
}
