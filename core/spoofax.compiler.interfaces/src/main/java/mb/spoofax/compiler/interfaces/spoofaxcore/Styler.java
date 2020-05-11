package mb.spoofax.compiler.interfaces.spoofaxcore;

import mb.common.style.Styling;
import mb.common.token.Token;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface Styler {
    Styling style(Iterable<? extends Token<IStrategoTerm>> tokens);
}
