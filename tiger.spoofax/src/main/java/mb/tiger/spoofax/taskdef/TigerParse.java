package mb.tiger.spoofax.taskdef;

import mb.common.message.Message;
import mb.common.message.Messages;
import mb.jsglr1.common.JSGLR1ParseResult;
import mb.jsglr1.common.JSGLR1ParseResults;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Provider;
import mb.pie.api.TaskDef;
import mb.tiger.TigerParser;

import javax.inject.Inject;
import java.io.IOException;

public class TigerParse implements TaskDef<Provider<String>, JSGLR1ParseResult> {
    private final javax.inject.Provider<TigerParser> parserProvider;

    @Inject
    public TigerParse(javax.inject.Provider<TigerParser> parserProvider) {
        this.parserProvider = parserProvider;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public JSGLR1ParseResult exec(ExecContext context, Provider<String> stringProvider) throws InterruptedException {
        final String text;
        try {
            text = context.require(stringProvider);
        } catch(ExecException | IOException e) {
            return JSGLR1ParseResults.failed(Messages.of(new Message("Cannot get text input for parser from '" + stringProvider + "'", e)));
        }
        final TigerParser parser = parserProvider.get();
        return parser.parse(text, "Module");
    }
}
