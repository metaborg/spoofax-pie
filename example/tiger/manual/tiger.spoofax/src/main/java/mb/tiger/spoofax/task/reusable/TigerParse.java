package mb.tiger.spoofax.task.reusable;

import mb.common.result.Result;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.jsglr1.pie.JSGLR1ParseTaskDef;
import mb.pie.api.ExecContext;
import mb.resource.ResourceKey;
import mb.tiger.TigerParser;
import mb.tiger.spoofax.TigerScope;
import org.checkerframework.checker.nullness.qual.Nullable;

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

    @Override protected Result<JSGLR1ParseOutput, JSGLR1ParseException> parse(ExecContext context, String text, @Nullable String startSymbol, @Nullable ResourceKey resource) throws InterruptedException {
        final TigerParser parser = parserProvider.get();
        try {
            return Result.ofOk(parser.parse(text, startSymbol != null ? startSymbol : "Module", resource));
        } catch(JSGLR1ParseException e) {
            return Result.ofErr(e);
        }
    }
}
