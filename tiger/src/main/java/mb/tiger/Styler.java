package mb.tiger;

import mb.common.style.Styling;
import mb.common.token.Token;
import mb.esv.common.ESVStyler;

public class Styler {
    private final ESVStyler styler;

    public Styler(StylingRules stylingRules) {
        this.styler = new ESVStyler(stylingRules.stylingRules);
    }

    public Styling style(Iterable<Token> tokens) {
        return styler.style(tokens);
    }
}
