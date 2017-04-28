package mb.pipe.run.core.model.message;

import java.io.Serializable;

public interface IMsgSeverity extends Serializable {
    void accept(MsgSeverityVisitor visitor, IMsg message);
}
