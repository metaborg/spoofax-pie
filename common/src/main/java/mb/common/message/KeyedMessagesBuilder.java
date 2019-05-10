package mb.common.message;

import mb.common.util.MultiHashMap;

import java.util.ArrayList;
import java.util.Collection;

public class KeyedMessagesBuilder<K> {
    private final MultiHashMap<K, Message> messages = new MultiHashMap<>();


    public void addMessage(K key, Message message) {
        this.messages.add(key, message);
    }


    public void addMessages(K key, Collection<? extends Message> messages) {
        this.messages.addAll(key, messages);
    }

    public void addMessages(K key, Messages messages) {
        this.messages.addAll(key, messages.messages);
    }

    public void addMessages(MultiHashMap<K, Message> messages) {
        this.messages.addAll(messages);
    }

    public void addMessages(KeyedMessages<K> keyedMessages) {
        this.messages.addAll(keyedMessages.messages);
    }


    public void replaceMessages(K key, ArrayList<Message> messages) {
        this.messages.replaceAll(key, messages);
    }

    public void replaceMessages(K key, Messages messages) {
        this.messages.replaceAll(key, messages.messages);
    }


    public void clear(K key) {
        this.messages.removeAll(key);
    }

    public void clearAll() {
        this.messages.clear();
    }


    public KeyedMessages<K> build() {
        return new KeyedMessages<>(new MultiHashMap<>(this.messages));
    }
}
