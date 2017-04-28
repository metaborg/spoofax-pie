package mb.pipe.run.core.model;

public class MsgConstants {
    public static final IMsgSeverity infoSeverity = new InfoMsgSeverity();
    public static final IMsgSeverity warningSeverity = new WarningMsgSeverity();
    public static final IMsgSeverity errorSeverity = new ErrorMsgSeverity();

    public static final IMsgType internalType = new InternalMsgType();
}
