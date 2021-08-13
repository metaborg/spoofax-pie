package mb.str.incr;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.resource.ResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.ResourceRuntimeException;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.strincr.message.MessageSeverity;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.jsglr2.messages.SourceRegion;

public class MessageConverter {
    public static void addMessagesToBuilder(KeyedMessagesBuilder messagesBuilder, Iterable<Message> messages, ResourceService resourceService) {
        for(Message message : messages) {
            final @Nullable SourceRegion sourceRegion = message.sourceRegion;
            final @Nullable Region region;
            if(sourceRegion != null) {
                region = Region.fromOffsets(sourceRegion.startOffset, sourceRegion.endOffset + 1, sourceRegion.startRow, sourceRegion.endRow + 1);
            } else {
                region = null;
            }
            @Nullable ResourceKey resourceKey;
            if(message.filename != null) {
                try {
                    resourceKey = resourceService.getResourceKey(ResourceKeyString.parse(message.filename));
                } catch(ResourceRuntimeException e) {
                    resourceKey = null;
                }
            } else {
                resourceKey = null;
            }
            final Severity severity = convertSeverity(message.severity);
            messagesBuilder.addMessage(message.getMessage(), severity, resourceKey, region);
        }
    }

    public static KeyedMessages convertMessages(ResourcePath rootDirectory, Iterable<Message> messages, ResourceService resourceService) {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        addMessagesToBuilder(messagesBuilder, messages, resourceService);
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
