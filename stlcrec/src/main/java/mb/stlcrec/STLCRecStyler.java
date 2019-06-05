package mb.stlcrec;

import mb.common.style.Styling;
import mb.common.token.Token;
import mb.esv.common.ESVStyler;

public class STLCRecStyler {
    private final ESVStyler styler;

    public STLCRecStyler(STLCRecStylingRules stylingRules) {
        this.styler = new ESVStyler(stylingRules.stylingRules);
    }

    public Styling style(Iterable<Token> tokens) {
        return styler.style(tokens);
    }
}
