package mb.spoofax.runtime.model.message;

import mb.pie.vfs.path.PPath;
import mb.spoofax.runtime.model.region.Region;

import javax.annotation.Nullable;

public class PathMsgImpl extends MsgImpl implements PathMsg {
    private static final long serialVersionUID = 1L;

    private final PPath path;


    public PathMsgImpl(String text, MsgSeverity severity, MsgType type, @Nullable Region region,
        @Nullable Throwable exception, PPath path) {
        super(text, severity, type, region, exception);
        this.path = path;
    }

    public PathMsgImpl(String text, MsgSeverity severity, MsgType type, @Nullable Region region, PPath path) {
        this(text, severity, type, region, null, path);
    }

    public PathMsgImpl(String text, MsgSeverity severity, MsgType type, PPath path) {
        this(text, severity, type, null, null, path);
    }


    @Override public PPath path() {
        return path;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + path.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(!super.equals(obj))
            return false;
        if(getClass() != obj.getClass())
            return false;
        final PathMsgImpl other = (PathMsgImpl) obj;
        if(!path.equals(other.path))
            return false;
        return true;
    }
}
