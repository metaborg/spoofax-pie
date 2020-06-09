package mb.common.result;

import mb.common.util.ListView;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class DefaultError implements Error {
    private final String description;
    private final @Nullable Error cause;
    private final @Nullable ListView<StackTraceElement> stackTrace;


    public DefaultError(String description, @Nullable Error cause, @Nullable ListView<StackTraceElement> stackTrace) {
        this.description = description;
        this.cause = cause;
        this.stackTrace = stackTrace;
    }

    public DefaultError(String description, @Nullable Error cause) {
        this(description, cause, null);
    }

    public DefaultError(String description, @Nullable Error cause, boolean createStackTrace) {
        this(description, cause, createStackTrace(createStackTrace));
    }

    public DefaultError(String description) {
        this(description, null, null);
    }

    public DefaultError(String description, boolean createStackTrace) {
        this(description, null, createStackTrace(createStackTrace));
    }


    @Override public String getDescription() {
        return description;
    }

    @Override public @Nullable Error getCause() {
        return cause;
    }

    @Override public @Nullable ListView<StackTraceElement> getStackTrace() {
        return stackTrace;
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final DefaultError that = (DefaultError)o;
        return description.equals(that.description) &&
            Objects.equals(cause, that.cause) &&
            Objects.equals(stackTrace, that.stackTrace);
    }

    @Override public int hashCode() {
        return Objects.hash(description, cause, stackTrace);
    }

    @Override public String toString() {
        return "DefaultError(" +
            "description='" + description + '\'' +
            ", cause=" + cause +
            ", stackTrace=" + stackTrace +
            ')';
    }

    protected static @Nullable ListView<StackTraceElement> createStackTrace(boolean createStackTrace) {
        if(createStackTrace) {
            // TODO: create an ArrayView to prevent copying the stacktrace?
            // TODO: should we pop the top element of the stacktrace so that this method does not show up?
            return ListView.of(new Throwable().getStackTrace());
        } else {
            return null;
        }
    }
}
