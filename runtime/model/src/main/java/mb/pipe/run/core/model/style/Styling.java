package mb.pipe.run.core.model.style;

import java.io.Serializable;
import java.util.List;

public interface Styling extends Serializable {
    List<TokenStyle> stylePerToken();
}
