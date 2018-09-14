package mb.spoofax.legacy;

import com.google.inject.Inject;
import mb.spoofax.api.SpoofaxRunEx;
import mb.spoofax.api.message.Message;
import mb.spoofax.api.message.Severity;
import mb.spoofax.api.region.Region;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.source.ISourceRegion;

import java.util.ArrayList;

public class MessageConverter {
    private final PathConverter pathConverter;


    @Inject public MessageConverter(PathConverter pathConverter) {
        this.pathConverter = pathConverter;
    }


    public Message toMessage(IMessage spoofaxCoreMessage) {
        final String text = spoofaxCoreMessage.message();
        final Severity severity = toSeverity(spoofaxCoreMessage.severity());
        final ISourceRegion spoofaxCoreRegion = spoofaxCoreMessage.region();
        final Region region = spoofaxCoreRegion != null ? RegionConverter.toRegion(spoofaxCoreRegion) : null;
        final Throwable exception = spoofaxCoreMessage.exception();
        final Message msg = new Message(text, severity, region, exception);
        return msg;
    }

    public ArrayList<Message> toMessages(Iterable<IMessage> spoofaxCoreMessages) {
        final ArrayList<Message> messages = new ArrayList<Message>();
        for(IMessage spoofaxCoreMessage : spoofaxCoreMessages) {
            final Message message = toMessage(spoofaxCoreMessage);
            messages.add(message);
        }
        return messages;
    }

    private static Severity toSeverity(MessageSeverity messageSeverity) {
        switch(messageSeverity) {
            case ERROR:
                return Severity.Error;
            case WARNING:
                return Severity.Warn;
            case NOTE:
                return Severity.Info;
            default:
                throw new SpoofaxRunEx("Cannot convert Spoofax Core message severity " + messageSeverity
                    + " to a pipeline message severity");
        }
    }
}
