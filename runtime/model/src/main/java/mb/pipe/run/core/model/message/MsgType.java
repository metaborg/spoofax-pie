package mb.pipe.run.core.model.message;

import java.io.Serializable;

public interface MsgType extends Serializable {
    void accept(MsgTypeVisitor visitor, Msg message);
}
