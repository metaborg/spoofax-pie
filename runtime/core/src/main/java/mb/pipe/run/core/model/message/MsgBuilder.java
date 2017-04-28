package mb.pipe.run.core.model.message;

import javax.annotation.Nullable;

import mb.pipe.run.core.model.region.IRegion;

public class MsgBuilder {
    private String text = "";
    private IMsgSeverity severity = MsgConstants.errorSeverity;
    private IMsgType type = MsgConstants.internalType;
    private @Nullable IRegion region;
    private @Nullable Throwable exception;


    public MsgBuilder() {
    }


    public MsgBuilder withText(String text) {
        this.text = text;
        return this;
    }


    public MsgBuilder withSeverity(IMsgSeverity severity) {
        this.severity = severity;
        return this;
    }

    public MsgBuilder asInfo() {
        this.severity = MsgConstants.infoSeverity;
        return this;
    }

    public MsgBuilder asWarning() {
        this.severity = MsgConstants.warningSeverity;
        return this;
    }

    public MsgBuilder asError() {
        this.severity = MsgConstants.errorSeverity;
        return this;
    }


    public MsgBuilder withType(IMsgType type) {
        this.type = type;
        return this;
    }

    public MsgBuilder asInternal() {
        this.type = MsgConstants.internalType;
        return this;
    }


    public MsgBuilder withRegion(IRegion region) {
        this.region = region;
        return this;
    }

    public MsgBuilder withoutRegion() {
        this.region = null;
        return this;
    }


    public MsgBuilder withException(Throwable exception) {
        this.exception = exception;
        return this;
    }

    public MsgBuilder withoutException() {
        this.exception = null;
        return this;
    }


    public IMsg build() {
        return new Msg(text, severity, type, region, exception);
    }
}
