package mb.spt.util;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.resource.ResourceKey;

public class SptMessageRemap {
    public static void addMessagesRemapped(KeyedMessagesBuilder messagesBuilder, ResourceKey source, ResourceKey target, KeyedMessages messages) {
        messagesBuilder.addMessages(target, messages.getMessagesOfKey(source));
        if(source.equals(messages.getResourceForMessagesWithoutKeys())) {
            messagesBuilder.addMessages(target, messages.getMessagesWithoutKey());
        }
    }
}
