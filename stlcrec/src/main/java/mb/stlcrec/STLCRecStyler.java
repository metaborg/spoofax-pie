package mb.stlcrec;

import mb.common.style.Styling;
import mb.common.token.Token;
import mb.esv.common.ESVStyler;
import mb.log.api.LoggerFactory;

public class STLCRecStyler {
    private final ESVStyler styler;

    public STLCRecStyler(STLCRecStylingRules stylingRules, LoggerFactory loggerFactory) {
        this.styler = new ESVStyler(stylingRules.stylingRules, loggerFactory);
    }

    public Styling style(Iterable<Token> tokens) {
        return styler.style(tokens);
    }
}
