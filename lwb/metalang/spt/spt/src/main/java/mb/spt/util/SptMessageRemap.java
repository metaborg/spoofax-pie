package mb.spt.util;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SptMessageRemap {
    public static void addMessagesRemapped(KeyedMessagesBuilder messagesBuilder, ResourceKey source, ResourceKey target, KeyedMessages messages) {
        messagesBuilder.addMessages(target, messages.getMessagesOfKey(source));
        final @Nullable ResourceKey fallbackResource = messages.getResourceForMessagesWithoutKeys();
        if(fallbackResource == null || source.equals(fallbackResource)) {
            messagesBuilder.addMessages(target, messages.getMessagesWithoutKey());
        }
    }
}
