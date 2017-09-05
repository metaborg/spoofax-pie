package mb.spoofax.runtime.model.style;

import java.io.Serializable;

import mb.spoofax.runtime.model.parse.Token;

public interface TokenStyle extends Serializable {
    Token token();

    Style style();
}
