package mb.spoofax.api.message;

import java.io.Serializable;

public interface MsgSeverity extends Serializable {
    <T> T accept(MsgSeverityVisitor<T> visitor, Msg message);
}
