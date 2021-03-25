package mb.jsglr1.pie;

import mb.common.message.KeyedMessages;
import mb.common.message.Messages;
import mb.common.result.Result;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseOutput;

class MessagesFunction extends MapperFunction<Result<JSGLR1ParseOutput, JSGLR1ParseException>, Messages> {
    public static final MessagesFunction instance = new MessagesFunction();

    @Override public Messages apply(Result<JSGLR1ParseOutput, JSGLR1ParseException> result) {
        // TODO: output KeyedMessages instead.
        return result.mapOrElse(v -> v.messages.asMessages(), e -> e.getOptionalMessages().map(KeyedMessages::asMessages).orElseGet(Messages::of));
    }

    private MessagesFunction() {}

    private Object readResolve() { return instance; }
}
