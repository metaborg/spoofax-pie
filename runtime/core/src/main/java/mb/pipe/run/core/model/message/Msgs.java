package mb.pipe.run.core.model.message;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Msgs {
    public static boolean containsErrors(Iterable<IMsg> messages) {
        return containsSeverity(messages, MsgConstants.errorSeverity);
    }

    public static boolean containsWarnings(Iterable<IMsg> messages) {
        return containsSeverity(messages, MsgConstants.warningSeverity);
    }

    public static boolean containsInfos(Iterable<IMsg> messages) {
        return containsSeverity(messages, MsgConstants.infoSeverity);
    }

    public static boolean containsSeverity(Iterable<IMsg> messages, IMsgSeverity severity) {
        return stream(messages).anyMatch(m -> m.severity().equals(severity));
    }


    public static boolean containsInternal(Iterable<IMsg> messages) {
        return containsType(messages, MsgConstants.internalType);
    }

    public static boolean containsType(Iterable<IMsg> messages, IMsgType type) {
        return stream(messages).anyMatch(m -> m.type().equals(type));
    }


    public static boolean containsException(Iterable<IMsg> messages) {
        return stream(messages).anyMatch(m -> m.exception() != null);
    }



    private static Stream<IMsg> stream(Iterable<IMsg> messages) {
        return StreamSupport.stream(messages.spliterator(), false);
    }
}
