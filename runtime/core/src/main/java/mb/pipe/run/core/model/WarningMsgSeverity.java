package mb.pipe.run.core.model;

public class WarningMsgSeverity implements IMsgSeverity {
    private static final long serialVersionUID = 1L;


    @Override public void accept(MsgSeverityVisitor visitor, IMsg message) {
        visitor.warning(message);
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
        return "warning";
    }
}
