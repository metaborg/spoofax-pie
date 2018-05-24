package mb.spoofax.api.message;

public interface MsgSeverityVisitor<T> {
    T info(Msg message);

    T warning(Msg message);

    T error(Msg message);
}
