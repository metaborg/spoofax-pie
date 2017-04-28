package mb.pipe.run.core.model;

public interface MsgTypeVisitor {
    void internal(IMsg message);

    void parse(IMsg message);
}
