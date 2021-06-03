package mb.jsglr.pie;

import mb.common.message.KeyedMessages;
import mb.common.message.Messages;
import mb.common.result.Result;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseOutput;

class MessagesFunction extends MapperFunction<Result<JsglrParseOutput, JsglrParseException>, Messages> {
    public static final MessagesFunction instance = new MessagesFunction();

    @Override public Messages apply(Result<JsglrParseOutput, JsglrParseException> result) {
        // TODO: output KeyedMessages instead.
        return result.mapOrElse(v -> v.messages.asMessages(), e -> e.getOptionalMessages().map(KeyedMessages::asMessages).orElseGet(Messages::of));
    }

    private MessagesFunction() {}

    private Object readResolve() { return instance; }
}
