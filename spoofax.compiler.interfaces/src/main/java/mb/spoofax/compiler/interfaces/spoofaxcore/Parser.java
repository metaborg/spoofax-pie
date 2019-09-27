package mb.spoofax.compiler.interfaces.spoofaxcore;

import mb.jsglr1.common.JSGLR1ParseResult;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface Parser {
    JSGLR1ParseResult parse(String text, String startSymbol) throws InterruptedException;

    JSGLR1ParseResult parse(String text, String startSymbol, @Nullable ResourceKey resource) throws InterruptedException;
}
