package mb.tiger.spoofax.task.reusable;

import mb.common.result.Result;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.jsglr1.pie.JSGLR1ParseTaskDef;
import mb.pie.api.ExecContext;
import mb.tiger.TigerParser;
import mb.tiger.spoofax.TigerScope;

import javax.inject.Inject;
import javax.inject.Provider;

@TigerScope
public class TigerParse extends JSGLR1ParseTaskDef {
    private final Provider<TigerParser> parserProvider;

    @Inject public TigerParse(Provider<TigerParser> parserProvider) {
        this.parserProvider = parserProvider;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override protected Result<JSGLR1ParseOutput, JSGLR1ParseException> parse(ExecContext context, String text) throws InterruptedException {
        final TigerParser parser = parserProvider.get();
        try {
            return Result.ofOk(parser.parse(text, "Module"));
        } catch(JSGLR1ParseException e) {
            return Result.ofErr(e);
        }
    }
}
