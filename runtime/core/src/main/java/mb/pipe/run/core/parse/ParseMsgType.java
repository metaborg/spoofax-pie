package mb.pipe.run.core.parse;

import mb.pipe.run.core.model.IMsg;
import mb.pipe.run.core.model.IMsgType;
import mb.pipe.run.core.model.MsgTypeVisitor;

public class ParseMsgType implements IMsgType {
    private static final long serialVersionUID = 1L;


    @Override public void accept(MsgTypeVisitor visitor, IMsg message) {
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
        return "parse";
    }
}
