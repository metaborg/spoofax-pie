package mb.spoofax.core.language.command;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Messages;
import mb.common.result.KeyedMessagesException;
import mb.common.result.MessagesException;
import mb.common.util.ListView;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class CommandFeedbackBuilder {
    private final ArrayList<ShowFeedback> showFeedbacks = new ArrayList<>();
    private final KeyedMessagesBuilder keyedMessagesBuilder = new KeyedMessagesBuilder();
    private @Nullable Exception mainException = null;


    public CommandFeedbackBuilder withShowFeedbacks(ArrayList<ShowFeedback> showFeedbacks) {
        this.showFeedbacks.clear();
        this.showFeedbacks.addAll(showFeedbacks);
        return this;
    }

    public CommandFeedbackBuilder addShowFeedback(ShowFeedback showFeedback) {
        this.showFeedbacks.add(showFeedback);
        return this;
    }

    public CommandFeedbackBuilder addAllShowFeedbacks(Collection<? extends ShowFeedback> showFeedbacks) {
        this.showFeedbacks.addAll(showFeedbacks);
        return this;
    }


    public CommandFeedbackBuilder withKeyedMessagesBuilder(KeyedMessagesBuilder keyedMessagesBuilder) {
        this.keyedMessagesBuilder.clearAll();
        this.keyedMessagesBuilder.addMessages(keyedMessagesBuilder);
        return this;
    }

    public CommandFeedbackBuilder withKeyedMessages(KeyedMessages keyedMessages) {
        this.keyedMessagesBuilder.clearAll();
        this.keyedMessagesBuilder.addMessages(keyedMessages);
        return this;
    }

    public CommandFeedbackBuilder withMessages(Messages messages) {
        this.keyedMessagesBuilder.clearAll();
        this.keyedMessagesBuilder.addMessages(messages);
        return this;
    }


    public CommandFeedbackBuilder withMainException(@Nullable Exception mainException) {
        this.mainException = mainException;
        return this;
    }


    public CommandFeedbackBuilder withException(Exception exception) {
        if(exception instanceof KeyedMessagesException) {
            final KeyedMessagesException keyedMessagesException = (KeyedMessagesException)exception;
            withKeyedMessages(keyedMessagesException.getMessages()); // TODO: default key?
        } else if(exception instanceof MessagesException) {
            final MessagesException messagesException = (MessagesException)exception;
            withMessages(messagesException.getMessages()); // TODO: key where messages belong to?
        } else {
            withMainException(exception);
        }
        return this;
    }


    public CommandFeedback build() {
        final ListView<ShowFeedback> showFeedbacks = ListView.of(this.showFeedbacks);
        final KeyedMessages keyedMessages = this.keyedMessagesBuilder.build();
        return new CommandFeedback(keyedMessages, mainException, showFeedbacks);
    }
}
