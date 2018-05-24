package mb.spoofax.api.message;

public interface MsgTypeVisitor {
    void internal(Msg message);

    void parse(Msg message);

    void namesAndTypes(Msg message);
}
