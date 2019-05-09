package mb.common.message;

import java.util.ArrayList;
import java.util.Collection;

public class MessageCollectionBuilder {
    private final ArrayList<Message> messages = new ArrayList<>();


    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public void addMessages(Collection<? extends Message> messages) {
        this.messages.addAll(messages);
    }


    public MessageCollection build() {
        return new MessageCollection(messages);
    }
}
