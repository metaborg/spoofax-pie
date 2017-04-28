package mb.pipe.run.core.model.message;

public interface MsgTypeVisitor {
    void internal(IMsg message);

    void parse(IMsg message);
}
