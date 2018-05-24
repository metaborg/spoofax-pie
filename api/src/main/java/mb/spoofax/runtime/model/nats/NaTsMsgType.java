package mb.spoofax.runtime.model.nats;

import mb.spoofax.runtime.model.message.Msg;
import mb.spoofax.runtime.model.message.MsgType;
import mb.spoofax.runtime.model.message.MsgTypeVisitor;

public class NaTsMsgType implements MsgType {
    private static final long serialVersionUID = 1L;


    @Override public void accept(MsgTypeVisitor visitor, Msg message) {
        visitor.parse(message);
    }


    @Override public int hashCode() {
        return 0;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        return true;
    }

    @Override public String toString() {
        return "name-and-types";
    }
}
