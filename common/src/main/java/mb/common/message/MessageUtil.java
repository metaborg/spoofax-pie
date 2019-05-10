package mb.common.message;

public class MessageUtil {
    public static boolean containsSeverity(Iterable<Message> messages, MessageSeverity severity) {
        for(Message message : messages) {
            if(message.severity.equals(severity)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsSeverityOrHigher(Iterable<Message> messages, MessageSeverity severity) {
        for(Message message : messages) {
            if(message.severity.compareTo(severity) >= 0) {
                return true;
            }
        }
        return false;
    }
}
