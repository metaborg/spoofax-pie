package mb.pipe.run.core.model.style;

import java.io.Serializable;
import java.util.List;

public interface IStyling extends Serializable {
    List<ITokenStyle> stylePerToken();
}
