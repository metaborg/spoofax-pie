package mb.spoofax.api.nats;

import mb.spoofax.api.message.Msg;
import mb.spoofax.api.message.MsgType;
import mb.spoofax.api.message.MsgTypeVisitor;

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
        return getClass() == obj.getClass();
    }

    @Override public String toString() {
        return "name-and-types";
    }
}
