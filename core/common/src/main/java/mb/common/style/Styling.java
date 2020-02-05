package mb.common.style;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public interface Styling extends Serializable {
    ArrayList<TokenStyle> getStylePerToken();
}
