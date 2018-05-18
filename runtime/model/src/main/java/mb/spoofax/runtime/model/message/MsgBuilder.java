package mb.spoofax.runtime.model.message;

import mb.pie.vfs.path.PPath;
import mb.spoofax.runtime.model.SpoofaxRunEx;
import mb.spoofax.runtime.model.region.Region;

import javax.annotation.Nullable;

public class MsgBuilder {
    private String text = "";
    private MsgSeverity severity = MsgConstants.errorSeverity;
    private MsgType type = MsgConstants.internalType;
    private @Nullable Region region;
    private @Nullable Throwable exception;
    private @Nullable PPath path;


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


    public MsgBuilder withPath(PPath path) {
        this.path = path;
        return this;
    }

    public MsgBuilder withoutPath() {
        this.path = null;
        return this;
    }


    public Msg build() {
        if(path != null) {
            return new PathMsgImpl(text, severity, type, region, exception, path);
        }
        return new MsgImpl(text, severity, type, region, exception);
    }

    public PathMsg buildWithPath() {
        if(path == null) {
            throw new SpoofaxRunEx("Cannot build a message with a path, since the path has not been set");
        }
        return new PathMsgImpl(text, severity, type, region, exception, path);
    }
}
