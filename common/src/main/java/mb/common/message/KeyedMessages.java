package mb.common.message;

import mb.common.util.MultiHashMap;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class KeyedMessages implements Serializable {
    final MultiHashMap<@Nullable ResourceKey, Message> messages;


    public KeyedMessages(MultiHashMap<@Nullable ResourceKey, Message> messages) {
        this.messages = messages;
    }


    public int size() {
        return messages.size();
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public void accept(KeyedMessageVisitor visitor) {
        for(Entry<@Nullable ResourceKey, ArrayList<Message>> entry : messages.entrySet()) {
            final @Nullable ResourceKey resource = entry.getKey();
            for(Message msg : entry.getValue()) {
                if(resource == null) {
                    if(!visitor.noOrigin(msg.text, msg.exception, msg.severity)) return;
                } else if(msg.region == null) {
                    if(!visitor.resourceOrigin(msg.text, msg.exception, msg.severity, resource)) return;
                } else {
                    if(!visitor.regionOrigin(msg.text, msg.exception, msg.severity, resource, msg.region)) return;
                }
            }
        }
    }

    public void accept(GeneralMessageVisitor visitor) {
        for(Entry<@Nullable ResourceKey, ArrayList<Message>> entry : messages.entrySet()) {
            final @Nullable ResourceKey resource = entry.getKey();
            for(Message msg : entry.getValue()) {
                if(!visitor.message(msg.text, msg.exception, msg.severity, resource, msg.region)) return;
            }
        }
    }

    public Set<@Nullable ResourceKey> getResources() {
        return messages.keySet();
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
        return new Messages(
            messages.values().stream().flatMap(Collection::stream).collect(Collectors.toCollection(ArrayList::new)));
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
}
