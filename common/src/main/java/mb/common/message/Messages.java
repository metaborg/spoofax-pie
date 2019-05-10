package mb.common.message;

import java.io.Serializable;
import java.util.ArrayList;

public class Messages implements Serializable {
    final ArrayList<Message> messages;


    public Messages(ArrayList<Message> list) {
        this.messages = list;
    }

    public Messages() {
        this.messages = new ArrayList<>();
    }


    public ArrayList<Message> getMessages() {
        return messages;
    }


    public boolean containsSeverity(MessageSeverity severity) {
        return MessageUtil.containsSeverity(messages, severity);
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
        return MessageUtil.containsSeverityOrHigher(messages, severity);
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
        final Messages that = (Messages) o;
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
