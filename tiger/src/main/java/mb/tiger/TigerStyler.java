package mb.tiger;

import mb.common.style.Styling;
import mb.common.token.Token;
import mb.esv.common.ESVStyler;
import mb.log.api.LoggerFactory;

public class TigerStyler {
    private final ESVStyler styler;

    public TigerStyler(TigerStylingRules stylingRules, LoggerFactory loggerFactory) {
        this.styler = new ESVStyler(stylingRules.stylingRules, loggerFactory);
    }

    public Styling style(Iterable<Token> tokens) {
        return styler.style(tokens);
    }
}
