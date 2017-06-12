package mb.pipe.run.core.model.message;

import javax.annotation.Nullable;

import mb.pipe.run.core.model.region.Region;

public class MsgBuilder {
    private String text = "";
    private MsgSeverity severity = MsgConstants.errorSeverity;
    private MsgType type = MsgConstants.internalType;
    private @Nullable Region region;
    private @Nullable Throwable exception;


    public MsgBuilder() {
    }


    public MsgBuilder withText(String text) {
        this.text = text;
        return this;
    }


    public MsgBuilder withSeverity(MsgSeverity severity) {
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


    public MsgBuilder withType(MsgType type) {
        this.type = type;
        return this;
    }

    public MsgBuilder asInternal() {
        this.type = MsgConstants.internalType;
        return this;
    }


    public MsgBuilder withRegion(Region region) {
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


    public Msg build() {
        return new MsgImpl(text, severity, type, region, exception);
    }
}
