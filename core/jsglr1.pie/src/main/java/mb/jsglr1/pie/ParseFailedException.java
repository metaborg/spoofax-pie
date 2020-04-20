package mb.jsglr1.pie;

import mb.common.message.Messages;

public class ParseFailedException extends RuntimeException {
    public final Messages messages;

    public ParseFailedException(Messages message) {
        super("Parsing failed");
        this.messages = message;
    }

    // TODO: override getMessage and print the messages from Messages.
}
