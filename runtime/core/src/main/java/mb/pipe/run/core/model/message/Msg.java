package mb.pipe.run.core.model.message;

import javax.annotation.Nullable;

import mb.pipe.run.core.model.region.IRegion;

public class Msg implements IMsg {
    private static final long serialVersionUID = 1L;

    private final String text;
    private final IMsgSeverity severity;
    private final IMsgType type;
    private final @Nullable IRegion region;
    private final @Nullable Throwable exception;


    public Msg(String text, IMsgSeverity severity, IMsgType type, @Nullable IRegion region,
        @Nullable Throwable exception) {
        this.text = text;
        this.severity = severity;
        this.type = type;
        this.region = region;
        this.exception = exception;
    }

    public Msg(String text, IMsgSeverity severity, IMsgType type, @Nullable IRegion region) {
        this(text, severity, type, region, null);
    }

    public Msg(String text, IMsgSeverity severity, IMsgType type) {
        this(text, severity, type, null, null);
    }


    @Override public String text() {
        return text;
    }

    @Override public IMsgSeverity severity() {
        return severity;
    }

    @Override public IMsgType type() {
        return type;
    }

    @Override public @Nullable IRegion region() {
        return region;
    }

    @Override public @Nullable Throwable exception() {
        return exception;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + text.hashCode();
        result = prime * result + severity.hashCode();
        result = prime * result + type.hashCode();
        result = prime * result + ((region == null) ? 0 : region.hashCode());
        result = prime * result + ((exception == null) ? 0 : exception.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final Msg other = (Msg) obj;
        if(!text.equals(other.text))
            return false;
        if(!severity.equals(other.severity))
            return false;
        if(!type.equals(other.type))
            return false;
        if(region == null) {
            if(other.region != null)
                return false;
        } else if(!region.equals(other.region))
            return false;
        if(exception == null) {
            if(other.exception != null)
                return false;
        } else if(!exception.equals(other.exception))
            return false;
        return true;
    }

    @Override public String toString() {
        return text;
    }
}
