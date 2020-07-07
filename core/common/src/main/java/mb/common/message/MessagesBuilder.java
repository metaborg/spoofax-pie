package mb.common.message;

import mb.common.region.Region;
import mb.common.util.CollectionView;
import mb.common.util.IterableUtil;
import mb.common.util.ListView;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class MessagesBuilder {
    private final ArrayList<Message> messages = new ArrayList<>();


    public void addMessage(String text, @Nullable Throwable exception, Severity severity, @Nullable Region region) {
        messages.add(new Message(text, exception, severity, region));
    }

    public void addMessage(String text, Severity severity, @Nullable Region region) {
        messages.add(new Message(text, severity, region));
    }

    public void addMessage(String text, @Nullable Throwable exception, Severity severity) {
        messages.add(new Message(text, exception, severity, null));
    }

    public void addMessage(String text, Severity severity) {
        messages.add(new Message(text, severity));
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }


    public void addMessages(Collection<? extends Message> messages) {
        this.messages.addAll(messages);
    }

    public void addMessages(CollectionView<Message> messages) {
        messages.addAllTo(this.messages);
    }

    public void addMessages(ListView<Message> messages) {
        messages.addAllTo(this.messages);
    }

    public void addMessages(Messages messages) {
        IterableUtil.addAll(this.messages, messages.messages);
    }


    public void replaceMessages(Collection<? extends Message> messages) {
        this.messages.clear();
        this.messages.addAll(messages);
    }

    public void replaceMessages(Messages messages) {
        this.messages.clear();
        IterableUtil.addAll(this.messages, messages.messages);
    }


    public void clearAll() {
        this.messages.clear();
    }


    public Messages build() {
        return new Messages(ListView.copyOf(messages));
    }
}
