package mb.spoofax.runtime.model.message;

public interface MsgTypeVisitor {
    void internal(Msg message);

    void parse(Msg message);

    void namesAndTypes(Msg message);
}
