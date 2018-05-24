package mb.spoofax.api.style;

import java.io.Serializable;

import mb.spoofax.api.parse.Token;

public interface TokenStyle extends Serializable {
    Token token();

    Style style();
}
