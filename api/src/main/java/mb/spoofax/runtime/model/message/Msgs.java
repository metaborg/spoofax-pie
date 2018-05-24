package mb.spoofax.runtime.model.message;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Msgs {
    public static boolean containsErrors(Iterable<Msg> messages) {
        return containsSeverity(messages, MsgConstants.errorSeverity);
    }

    public static boolean containsWarnings(Iterable<Msg> messages) {
        return containsSeverity(messages, MsgConstants.warningSeverity);
    }

    public static boolean containsInfos(Iterable<Msg> messages) {
        return containsSeverity(messages, MsgConstants.infoSeverity);
    }

    public static boolean containsSeverity(Iterable<Msg> messages, MsgSeverity severity) {
        return stream(messages).anyMatch(m -> m.severity().equals(severity));
    }


    public static boolean containsInternal(Iterable<Msg> messages) {
        return containsType(messages, MsgConstants.internalType);
    }

    public static boolean containsType(Iterable<Msg> messages, MsgType type) {
        return stream(messages).anyMatch(m -> m.type().equals(type));
    }


    public static boolean containsException(Iterable<Msg> messages) {
        return stream(messages).anyMatch(m -> m.exception() != null);
    }



    private static Stream<Msg> stream(Iterable<Msg> messages) {
        return StreamSupport.stream(messages.spliterator(), false);
    }
}
