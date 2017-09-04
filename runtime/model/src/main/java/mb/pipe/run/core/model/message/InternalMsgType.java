package mb.pipe.run.core.model.message;

public class InternalMsgType implements MsgType {
    private static final long serialVersionUID = 1L;

    
    @Override public void accept(MsgTypeVisitor visitor, Msg message) {
        visitor.internal(message);
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
        return "internal";
    }
}
