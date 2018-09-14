package mb.spoofax.api.message;

import mb.pie.vfs.path.PPath;
import mb.spoofax.api.util.MultiHashMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class MessageCollection implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ArrayList<Message> globalMessages = new ArrayList<>();
    private final MultiHashMap<PPath, Message> containerMessages = new MultiHashMap<>();
    private final MultiHashMap<PPath, Message> documentMessages = new MultiHashMap<>();


    public ArrayList<Message> globalMessages() {
        return globalMessages;
    }

    public ArrayList<Message> messagesForContainer(PPath container) {
        return containerMessages.get(container);
    }

    public MultiHashMap<PPath, Message> containerMessages() {
        return containerMessages;
    }

    public ArrayList<Message> messagesForDocument(PPath document) {
        return documentMessages.get(document);
    }

    public MultiHashMap<PPath, Message> documentMessages() {
        return documentMessages;
    }


    public boolean containsSeverity(Severity severity) {
        if(MessageUtils.containsSeverity(globalMessages, severity)) {
            return true;
        }
        for(ArrayList<Message> messages : containerMessages.values()) {
            if(MessageUtils.containsSeverity(messages, severity)) {
                return true;
            }
        }
        for(ArrayList<Message> messages : documentMessages.values()) {
            if(MessageUtils.containsSeverity(messages, severity)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsError() {
        return containsSeverity(Severity.Error);
    }

    public boolean containsWarning() {
        return containsSeverity(Severity.Warn);
    }


    public boolean containsSeverityOrHigher(Severity severity) {
        if(MessageUtils.containsSeverityOrHigher(globalMessages, severity)) {
            return true;
        }
        for(ArrayList<Message> messages : containerMessages.values()) {
            if(MessageUtils.containsSeverityOrHigher(messages, severity)) {
                return true;
            }
        }
        for(ArrayList<Message> messages : documentMessages.values()) {
            if(MessageUtils.containsSeverityOrHigher(messages, severity)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsWarningOrHigher() {
        return containsSeverityOrHigher(Severity.Warn);
    }


    public void addGlobalMessage(Message message) {
        globalMessages.add(message);
    }

    public void addContainerMessage(PPath container, Message message) {
        containerMessages.add(container, message);
    }

    public void addDocumentMessage(PPath document, Message message) {
        documentMessages.add(document, message);
    }


    public void addGlobalMessages(Collection<Message> messages) {
        globalMessages.addAll(messages);
    }

    public void addContainerMessages(PPath container, Collection<Message> messages) {
        containerMessages.addAll(container, messages);
    }

    public void addDocumentMessages(PPath document, Collection<Message> messages) {
        documentMessages.addAll(document, messages);
    }


    public void addMessagesFrom(MessageCollection other) {
        globalMessages.addAll(other.globalMessages);
        containerMessages.addAll(other.containerMessages);
        documentMessages.addAll(other.documentMessages);
    }


    public void clearGlobalMessages() {
        globalMessages.clear();
    }

    public void clearContainerMessages(PPath container) {
        final ArrayList<Message> messages = containerMessages.get(container);
        if(messages != null) {
            messages.clear();
        }
    }

    public void clearDocumentMessages(PPath document) {
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
        final MessageCollection that = (MessageCollection) o;
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
