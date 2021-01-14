package mb.common.style;

import mb.common.token.Token;

import java.io.Serializable;

public interface TokenStyle extends Serializable {
    Token<?> getToken();

    Style getStyle();
}
