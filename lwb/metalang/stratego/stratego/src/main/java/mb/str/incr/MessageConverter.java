package mb.str.incr;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.jsglr.common.TermTracer;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.message.MessageSeverity;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MessageConverter {
    public static void addMessagesToBuilder(KeyedMessagesBuilder messagesBuilder, Iterable<Message> messages) {
        for(Message message : messages) {
            final @Nullable Region region = TermTracer.getRegion(message.locationTerm);
            final @Nullable ResourceKey resourceKey = TermTracer.getResourceKey(message.locationTerm);
            final Severity severity = convertSeverity(message.severity);
            messagesBuilder.addMessage(message.getMessage(), severity, resourceKey, region);
        }
    }

    public static KeyedMessages convertMessages(ResourcePath rootDirectory, Iterable<Message> messages) {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        addMessagesToBuilder(messagesBuilder, messages);
        return messagesBuilder.build(rootDirectory);
    }

    private static Severity convertSeverity(MessageSeverity severity) {
        switch(severity) {
            case NOTE:
                return Severity.Info;
            case WARNING:
                return Severity.Warning;
            case ERROR:
                return Severity.Error;
        }
        return Severity.Error;
    }
}
