package mb.spoofax.api.message;

import mb.spoofax.api.region.Region;

import javax.annotation.Nullable;
import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public final String text;
    public final Severity severity;
    public final @Nullable Region region;
    public final @Nullable Throwable exception;


    public Message(String text, Severity severity, @Nullable Region region, @Nullable Throwable exception) {
        this.text = text;
        this.severity = severity;
        this.region = region;
        this.exception = exception;
    }

    public Message(String text, Severity severity, @Nullable Region region) {
        this(text, severity, region, null);
    }

    public Message(String text, Severity severity) {
        this(text, severity, null, null);
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Message message = (Message) o;
        if(!text.equals(message.text)) return false;
        if(severity != message.severity) return false;
        if(region != null ? !region.equals(message.region) : message.region != null) return false;
        return exception != null ? exception.equals(message.exception) : message.exception == null;
    }

    @Override public int hashCode() {
        int result = text.hashCode();
        result = 31 * result + severity.hashCode();
        result = 31 * result + (region != null ? region.hashCode() : 0);
        result = 31 * result + (exception != null ? exception.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return text;
    }
}
