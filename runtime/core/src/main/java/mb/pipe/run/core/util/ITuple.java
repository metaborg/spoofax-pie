package mb.pipe.run.core.util;

import java.io.Serializable;

public interface ITuple extends Serializable {
    Object get(int index);
}
