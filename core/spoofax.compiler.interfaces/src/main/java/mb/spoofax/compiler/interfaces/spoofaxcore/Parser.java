package mb.spoofax.compiler.interfaces.spoofaxcore;

import mb.jsglr1.common.JSGLR1ParseException;
import mb.jsglr1.common.JSGLR1ParseInput;
import mb.jsglr1.common.JSGLR1ParseOutput;

public interface Parser {
    JSGLR1ParseOutput parse(JSGLR1ParseInput input) throws JSGLR1ParseException, InterruptedException;
}
