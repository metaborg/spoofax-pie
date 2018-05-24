package mb.spoofax.api.style;

import java.io.Serializable;
import java.util.List;

public interface Styling extends Serializable {
    List<TokenStyle> stylePerToken();
}
