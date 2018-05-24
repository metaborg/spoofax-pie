package mb.spoofax.api.message;

public class ErrorMsgSeverity implements MsgSeverity {
    private static final long serialVersionUID = 1L;


    @Override public <T> T accept(MsgSeverityVisitor<T> visitor, Msg message) {
        return visitor.error(message);
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
        return "error";
    }
}
