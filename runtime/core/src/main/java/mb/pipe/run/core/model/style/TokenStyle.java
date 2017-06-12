package mb.pipe.run.core.model.style;

import java.io.Serializable;

import mb.pipe.run.core.model.parse.Token;

public interface TokenStyle extends Serializable {
    Token token();

    Style style();
}
