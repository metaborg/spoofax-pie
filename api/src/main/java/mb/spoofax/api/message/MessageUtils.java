package mb.spoofax.api.message;

public class MessageUtils {
    public static boolean containsSeverity(Iterable<Message> messages, Severity severity) {
        for(Message message : messages) {
            if(message.severity.equals(severity)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsError(Iterable<Message> messages) {
        return containsSeverity(messages, Severity.Error);
    }

    public static boolean containsWarning(Iterable<Message> messages) {
        return containsSeverity(messages, Severity.Warn);
    }


    public static boolean containsSeverityOrHigher(Iterable<Message> messages, Severity severity) {
        for(Message message : messages) {
            if(message.severity.compareTo(severity) >= 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsWarningOrHigher(Iterable<Message> messages) {
        return containsSeverityOrHigher(messages, Severity.Error);
    }
}
