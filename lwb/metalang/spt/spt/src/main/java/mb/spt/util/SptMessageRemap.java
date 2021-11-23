package mb.spt.util;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Message;
import mb.common.region.Region;
import mb.common.text.FragmentedString;
import mb.common.text.StringFragment;
import mb.resource.ResourceKey;
import mb.spt.model.TestCase;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SptMessageRemap {
    public static void addMessagesRemapped(KeyedMessagesBuilder messagesBuilder, TestCase testCase, ResourceKey target, @Nullable Region fallbackRegion, KeyedMessages messages) {
        addMessagesRemapped(messagesBuilder, testCase.resource, testCase.testFragment.getFragmentedString(), target, fallbackRegion, messages);
    }

    public static void addMessagesRemapped(KeyedMessagesBuilder messagesBuilder, ResourceKey source, FragmentedString fragmentedString, ResourceKey target, @Nullable Region fallbackRegion, KeyedMessages messages) {
        for(Message message : messages.getMessagesOfKey(source)) {
            messagesBuilder.addMessage(constrainRegion(message, fragmentedString, fallbackRegion), target);
        }

        final @Nullable ResourceKey fallbackResource = messages.getResourceForMessagesWithoutKeys();
        if(fallbackResource == null || source.equals(fallbackResource)) {
            for(Message message : messages.getMessagesWithoutKey()) {
                messagesBuilder.addMessage(constrainRegion(message, fragmentedString, fallbackRegion), target);
            }
        }
    }

    public static Message constrainRegion(Message message, FragmentedString fragmentedString, @Nullable Region fallbackRegion) {
        if(fallbackRegion == null) return message;
        final @Nullable Region region = message.region;
        if(region == null) return new Message(message.text, message.exception, message.severity, fallbackRegion);
        for(StringFragment fragment : fragmentedString.fragments) {
            final Region fragmentRegion = Region.fromOffsetLength(fragment.startOffset, fragment.text.length());
            if(fragmentRegion.contains(region)) return message;
        }
        return new Message(message.text, message.exception, message.severity, fallbackRegion);
    }
}
