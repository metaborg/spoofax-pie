package mb.spoofax.api.message;

import javax.annotation.Nullable;

import mb.spoofax.api.region.Region;

public class MsgImpl implements Msg {
    private static final long serialVersionUID = 1L;

    private final String text;
    private final MsgSeverity severity;
    private final MsgType type;
    private final @Nullable
    Region region;
    private final @Nullable Throwable exception;


    public MsgImpl(String text, MsgSeverity severity, MsgType type, @Nullable Region region,
        @Nullable Throwable exception) {
        this.text = text;
        this.severity = severity;
        this.type = type;
        this.region = region;
        this.exception = exception;
    }

    public MsgImpl(String text, MsgSeverity severity, MsgType type, @Nullable Region region) {
        this(text, severity, type, region, null);
    }

    public MsgImpl(String text, MsgSeverity severity, MsgType type) {
        this(text, severity, type, null, null);
    }


    @Override public String text() {
        return text;
    }

    @Override public MsgSeverity severity() {
        return severity;
    }

    @Override public MsgType type() {
        return type;
    }

    @Override public @Nullable Region region() {
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
        final MsgImpl other = (MsgImpl) obj;
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
            return other.exception == null;
        } else return exception.equals(other.exception);
    }

    @Override public String toString() {
        return text;
    }
}
