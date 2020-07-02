package mb.common.message;

import mb.common.region.Region;
import mb.common.util.ListView;
import mb.common.util.MapView;
import mb.common.util.MultiHashMap;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class KeyedMessagesBuilder {
    private final MultiHashMap<ResourceKey, Message> messages = new MultiHashMap<>();
    private final ArrayList<Message> messagesWithoutKey = new ArrayList<>();


    public void addMessage(String text, @Nullable Throwable exception, Severity severity, ResourceKey resourceKey, @Nullable Region region) {
        messages.put(resourceKey, new Message(text, exception, severity, region));
    }

    public void addMessage(String text, Severity severity, ResourceKey resourceKey, @Nullable Region region) {
        messages.put(resourceKey, new Message(text, severity, region));
    }

    public void addMessage(String text, @Nullable Throwable exception, Severity severity, ResourceKey resourceKey) {
        messages.put(resourceKey, new Message(text, exception, severity, null));
    }

    public void addMessage(String text, Severity severity, ResourceKey resourceKey) {
        messages.put(resourceKey, new Message(text, severity));
    }

    public void addMessage(String text, @Nullable Throwable exception, Severity severity, @Nullable Region region) {
        messagesWithoutKey.add(new Message(text, exception, severity, region));
    }

    public void addMessage(String text, @Nullable Throwable exception, Severity severity) {
        messagesWithoutKey.add(new Message(text, exception, severity, null));
    }

    public void addMessage(String text, Severity severity) {
        messagesWithoutKey.add(new Message(text, severity));
    }

    public void addMessage(Message message, ResourceKey resourceKey) {
        this.messages.put(resourceKey, message);
    }

    public void addMessage(Message message) {
        this.messagesWithoutKey.add(message);
    }


    public void addMessages(Collection<? extends Message> messages) {
        this.messagesWithoutKey.addAll(messages);
    }

    public void addMessages(ResourceKey resourceKey, Iterable<? extends Message> messages) {
        this.messages.putAll(resourceKey, messages);
    }

    public void addMessages(ResourceKey resourceKey, Messages messages) {
        this.messages.putAll(resourceKey, messages.messages);
    }

    public void addMessages(MultiHashMap<ResourceKey, Message> messages) {
        this.messages.putAll(messages);
    }

    public void addMessages(Messages messages) {
        messages.messages.addAllTo(this.messagesWithoutKey);
    }

    public void addMessages(KeyedMessages keyedMessages) {
        for(Map.Entry<ResourceKey, ArrayList<Message>> entry : keyedMessages.messages) {
            this.messages.putAll(entry.getKey(), entry.getValue());
        }
        keyedMessages.messagesWithoutKey.addAllTo(this.messagesWithoutKey);
    }

    public void addMessagesWithDefaultKey(KeyedMessages keyedMessages, ResourceKey defaultKey) {
        for(Map.Entry<ResourceKey, ArrayList<Message>> entry : keyedMessages.messages) {
            this.messages.putAll(entry.getKey(), entry.getValue());
        }
        this.messages.putAll(defaultKey, keyedMessages.messagesWithoutKey);
    }

    public void addMessages(KeyedMessagesBuilder keyedMessagesBuilder) {
        this.messages.putAll(keyedMessagesBuilder.messages);
        this.messagesWithoutKey.addAll(keyedMessagesBuilder.messagesWithoutKey);
    }

    public void addMessagesWithDefaultKey(KeyedMessagesBuilder keyedMessagesBuilder, ResourceKey defaultKey) {
        this.messages.putAll(keyedMessagesBuilder.messages);
        this.messages.putAll(defaultKey, keyedMessagesBuilder.messagesWithoutKey);
    }


    public void replaceMessages(ResourceKey resourceKey, Iterable<? extends Message> messages) {
        this.messages.removeAll(resourceKey);
        this.messages.putAll(resourceKey, messages);
    }

    public void replaceMessages(ResourceKey resourceKey, ArrayList<Message> messages) {
        this.messages.replaceAll(resourceKey, messages);
    }

    public void replaceMessages(ResourceKey resourceKey, Messages messages) {
        this.messages.removeAll(resourceKey);
        this.messages.putAll(resourceKey, messages.messages);
    }

    public void replaceMessages(KeyedMessages keyedMessages) {
        this.messages.replaceAll(keyedMessages.messages.asUnmodifiable());
    }


    public void clear(ResourceKey resourceKey) {
        messages.removeAll(resourceKey);
    }

    public void clearWithoutKey() {
        messagesWithoutKey.clear();
    }

    public void clearAll() {
        messages.clear();
        messagesWithoutKey.clear();
    }


    public KeyedMessages build() {
        return new KeyedMessages(MapView.copyOf(messages.getInnerMap()), ListView.copyOf(messagesWithoutKey));
    }

    public KeyedMessages build(ResourceKey defaultKey) {
        return new KeyedMessages(MapView.copyOf(messages.getInnerMap()), ListView.copyOf(messagesWithoutKey));
    }
}
