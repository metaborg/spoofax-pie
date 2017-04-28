package mb.pipe.run.core.model;

import java.io.Serializable;

public interface IMsgType extends Serializable {
    void accept(MsgTypeVisitor visitor, IMsg message);
}
