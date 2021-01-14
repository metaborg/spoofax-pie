package mb.common.message;

import mb.common.region.Region;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public final String text;
    public final Severity severity;
    public final @Nullable Region region;
    public final @Nullable Throwable exception;


    public Message(String text, @Nullable Throwable exception, Severity severity, @Nullable Region region) {
        this.text = text;
        this.exception = exception;
        this.severity = severity;
        this.region = region;
    }

    public Message(String text, Severity severity, @Nullable Region region) {
        this(text, null, severity, region);
    }

    public Message(String text, Throwable exception, Severity severity) {
        this(text, exception, severity, null);
    }

    public Message(String text, Severity severity) {
        this(text, null, severity, null);
    }

    public Message(String text, Throwable exception) {
        this(text, exception, Severity.Error, null);
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Message message = (Message) o;
        return text.equals(message.text) &&
            severity == message.severity &&
            Objects.equals(region, message.region) &&
            Objects.equals(exception, message.exception);
    }

    @Override public int hashCode() {
        return Objects.hash(text, severity, region, exception);
    }

    @Override public String toString() {
        return text;
    }
}
