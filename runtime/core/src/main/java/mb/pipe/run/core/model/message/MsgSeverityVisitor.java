package mb.pipe.run.core.model.message;

public interface MsgSeverityVisitor {
    void info(IMsg message);

    void warning(IMsg message);

    void error(IMsg message);
}
