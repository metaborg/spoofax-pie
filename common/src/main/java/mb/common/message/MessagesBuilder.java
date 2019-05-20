package mb.common.message;

import mb.common.region.Region;
import mb.common.util.MultiHashMap;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class MessagesBuilder {
    private final MultiHashMap<@Nullable ResourceKey, Message> messages = new MultiHashMap<>();


    public void addMessage(String text, @Nullable Throwable exception, Severity severity, @Nullable ResourceKey resourceKey, @Nullable Region region) {
        messages.add(resourceKey, new Message(text, exception, severity, region));
    }

    public void addMessage(String text, Severity severity, @Nullable ResourceKey resourceKey, @Nullable Region region) {
        messages.add(resourceKey, new Message(text, severity, region));
    }

    public void addMessage(String text, @Nullable Throwable exception, Severity severity, @Nullable ResourceKey resourceKey) {
        messages.add(resourceKey, new Message(text, exception, severity, null));
    }

    public void addMessage(String text, Severity severity, @Nullable ResourceKey resourceKey) {
        messages.add(resourceKey, new Message(text, severity));
    }

    public void addMessage(String text, @Nullable Throwable exception, Severity severity) {
        messages.add(null, new Message(text, exception, severity, null));
    }

    public void addMessage(String text, Severity severity) {
        messages.add(null, new Message(text, severity));
    }


    public void addMessage(Message message) {
        this.messages.add(null, message);
    }

    public void addMessages(Collection<? extends Message> messages) {
        this.messages.addAll(null, messages);
    }


    public void addMessage(ResourceKey resourceKey, Message message) {
        this.messages.add(resourceKey, message);
    }

    public void addMessages(ResourceKey resourceKey, Collection<? extends Message> messages) {
        this.messages.addAll(resourceKey, messages);
    }

    public void addMessages(MultiHashMap<ResourceKey, Message> messages) {
        this.messages.addAll(messages);
    }

    public void addMessages(Messages messages) {
        this.messages.addAll(messages.messages);
    }


    public void replaceMessages(ResourceKey resourceKey, ArrayList<Message> messages) {
        this.messages.replaceAll(resourceKey, messages);
    }

    public void replaceMessages(Messages messages) {
        this.messages.replaceAll(messages.messages);
    }


    public void clear(ResourceKey resourceKey) {
        this.messages.removeAll(resourceKey);
    }

    public void clearAll() {
        this.messages.clear();
    }


    public Messages build() {
        return new Messages(new MultiHashMap<>(this.messages));
    }
}
