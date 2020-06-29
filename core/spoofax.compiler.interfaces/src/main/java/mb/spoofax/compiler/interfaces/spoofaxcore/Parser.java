package mb.spoofax.compiler.interfaces.spoofaxcore;

import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface Parser {
    default JSGLR1ParseOutput parse(String text, String startSymbol) throws JSGLR1ParseException, InterruptedException {
        return parse(text, startSymbol, null);
    }

    JSGLR1ParseOutput parse(String text, String startSymbol, @Nullable ResourceKey resource) throws JSGLR1ParseException, InterruptedException;
}
