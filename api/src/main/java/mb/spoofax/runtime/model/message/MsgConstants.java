package mb.spoofax.runtime.model.message;

public class MsgConstants {
    public static final MsgSeverity infoSeverity = new InfoMsgSeverity();
    public static final MsgSeverity warningSeverity = new WarningMsgSeverity();
    public static final MsgSeverity errorSeverity = new ErrorMsgSeverity();

    public static final MsgType internalType = new InternalMsgType();
}
