package mb.common.token;

import java.io.Serializable;
import java.util.ArrayList;

public interface Tokens<F> extends Serializable {
    ArrayList<? extends Token<F>> getTokens(); // ArrayList required because of Serializable.
}
