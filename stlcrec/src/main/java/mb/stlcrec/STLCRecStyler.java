package mb.stlcrec;

import mb.common.style.Styling;
import mb.common.token.Token;
import mb.esv.common.ESVStyler;
import mb.log.api.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class STLCRecStyler {
    private final ESVStyler styler;

    public STLCRecStyler(STLCRecStylingRules stylingRules, LoggerFactory loggerFactory) {
        this.styler = new ESVStyler(stylingRules.stylingRules, loggerFactory);
    }

    public Styling style(Iterable<? extends Token<IStrategoTerm>> tokens) {
        return styler.style(tokens);
    }
}
