package mb.pipe.run.core.model.message;

public interface MsgTypeVisitor {
    void internal(Msg message);

    void parse(Msg message);
}
