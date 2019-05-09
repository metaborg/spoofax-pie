package mb.common.message;

import mb.common.util.MultiHashMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class KeyedMessageCollection implements Serializable {
    private final ArrayList<Message> globalMessages = new ArrayList<>();
    private final MultiHashMap<String, Message> containerMessages = new MultiHashMap<>();
    private final MultiHashMap<String, Message> documentMessages = new MultiHashMap<>();


    public ArrayList<Message> globalMessages() {
        return globalMessages;
    }

    public ArrayList<Message> messagesForContainer(String container) {
        return containerMessages.get(container);
    }

    public MultiHashMap<String, Message> containerMessages() {
        return containerMessages;
    }

    public ArrayList<Message> messagesForDocument(String document) {
        return documentMessages.get(document);
    }

    public MultiHashMap<String, Message> documentMessages() {
        return documentMessages;
    }


    public boolean containsSeverity(MessageSeverity severity) {
        if(MessageUtil.containsSeverity(globalMessages, severity)) {
            return true;
        }
        for(ArrayList<Message> messages : containerMessages.values()) {
            if(MessageUtil.containsSeverity(messages, severity)) {
                return true;
            }
        }
        for(ArrayList<Message> messages : documentMessages.values()) {
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


    public boolean containsSeverityOrHigher(MessageSeverity severity) {
        if(MessageUtil.containsSeverityOrHigher(globalMessages, severity)) {
            return true;
        }
        for(ArrayList<Message> messages : containerMessages.values()) {
            if(MessageUtil.containsSeverityOrHigher(messages, severity)) {
                return true;
            }
        }
        for(ArrayList<Message> messages : documentMessages.values()) {
            if(MessageUtil.containsSeverityOrHigher(messages, severity)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsWarningOrHigher() {
        return containsSeverityOrHigher(MessageSeverity.Warn);
    }


    public void addGlobalMessage(Message message) {
        globalMessages.add(message);
    }

    public void addContainerMessage(String container, Message message) {
        containerMessages.add(container, message);
    }

    public void addDocumentMessage(String document, Message message) {
        documentMessages.add(document, message);
    }


    public void addGlobalMessages(Collection<Message> messages) {
        globalMessages.addAll(messages);
    }

    public void addContainerMessages(String container, Collection<Message> messages) {
        containerMessages.addAll(container, messages);
    }

    public void addDocumentMessages(String document, Collection<Message> messages) {
        documentMessages.addAll(document, messages);
    }


    public void addMessagesFrom(KeyedMessageCollection other) {
        globalMessages.addAll(other.globalMessages);
        containerMessages.addAll(other.containerMessages);
        documentMessages.addAll(other.documentMessages);
    }


    public void clearGlobalMessages() {
        globalMessages.clear();
    }

    public void clearContainerMessages(String container) {
        final ArrayList<Message> messages = containerMessages.get(container);
        if(messages != null) {
            messages.clear();
        }
    }

    public void clearDocumentMessages(String document) {
        final ArrayList<Message> messages = documentMessages.get(document);
        if(messages != null) {
            messages.clear();
        }
    }

    public void clearAllMessages() {
        globalMessages.clear();
        containerMessages.clear();
        documentMessages.clear();
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final KeyedMessageCollection that = (KeyedMessageCollection) o;
        if(!globalMessages.equals(that.globalMessages)) return false;
        if(!containerMessages.equals(that.containerMessages)) return false;
        return documentMessages.equals(that.documentMessages);
    }

    @Override public int hashCode() {
        int result = globalMessages.hashCode();
        result = 31 * result + containerMessages.hashCode();
        result = 31 * result + documentMessages.hashCode();
        return result;
    }

    @Override public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        globalMessages.forEach(m -> {
            stringBuilder.append(m.toString());
            stringBuilder.append('\n');
        });
        containerMessages.forEach((c, ms) -> {
            stringBuilder.append(c.toString());
            stringBuilder.append(":\n");
            ms.forEach(m -> {
                stringBuilder.append("  ");
                stringBuilder.append(m.toString());
                stringBuilder.append('\n');
            });
        });
        documentMessages.forEach((d, ms) -> {
            stringBuilder.append('\n');
            stringBuilder.append(d.toString());
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
