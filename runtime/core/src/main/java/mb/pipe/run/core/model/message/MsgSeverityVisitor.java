package mb.pipe.run.core.model.message;

public interface MsgSeverityVisitor<T> {
    T info(IMsg message);

    T warning(IMsg message);

    T error(IMsg message);
}
