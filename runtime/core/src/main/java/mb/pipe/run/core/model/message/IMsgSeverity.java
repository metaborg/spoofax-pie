package mb.pipe.run.core.model.message;

import java.io.Serializable;

public interface IMsgSeverity extends Serializable {
    <T> T accept(MsgSeverityVisitor<T> visitor, IMsg message);
}
