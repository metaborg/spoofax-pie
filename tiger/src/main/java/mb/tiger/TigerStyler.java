package mb.tiger;

import mb.common.style.Styling;
import mb.common.token.Token;
import mb.esv.common.ESVStyler;

public class TigerStyler {
    private final ESVStyler styler;

    public TigerStyler(TigerStylingRules stylingRules) {
        this.styler = new ESVStyler(stylingRules.stylingRules);
    }

    public Styling style(Iterable<Token> tokens) {
        return styler.style(tokens);
    }
}
