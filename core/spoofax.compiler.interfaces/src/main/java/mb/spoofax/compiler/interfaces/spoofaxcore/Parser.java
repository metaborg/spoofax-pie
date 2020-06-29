package mb.spoofax.compiler.interfaces.spoofaxcore;

import mb.common.result.Result;
import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface Parser {
    default Result<JSGLR1ParseOutput, JSGLR1ParseException> parse(String text, String startSymbol) throws InterruptedException {
        return parse(text, startSymbol, null);
    }

    Result<JSGLR1ParseOutput, JSGLR1ParseException> parse(String text, String startSymbol, @Nullable ResourceKey resource) throws InterruptedException;
}
