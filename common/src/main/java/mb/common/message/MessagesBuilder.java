package mb.common.message;

import java.util.ArrayList;
import java.util.Collection;

public class MessagesBuilder {
    private final ArrayList<Message> messages = new ArrayList<>();


    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public void addMessages(Collection<? extends Message> messages) {
        this.messages.addAll(messages);
    }

    public void addMessages(Messages messages) {
        this.messages.addAll(messages.messages);
    }


    public void clearAll() {
        this.messages.clear();
    }


    public Messages build() {
        return new Messages(new ArrayList<>(messages));
    }
}
