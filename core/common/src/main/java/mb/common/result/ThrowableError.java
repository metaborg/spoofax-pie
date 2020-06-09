package mb.common.result;

import mb.common.util.ListView;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ThrowableError implements Error {
    private final Throwable throwable;


    public ThrowableError(Throwable throwable) {
        this.throwable = throwable;
    }


    public Throwable getThrowable() {
        return throwable;
    }

    public void doThrow() throws Throwable {
        throw throwable;
    }


    @Override public String getDescription() {
        return throwable.getMessage();
    }

    @Override public @Nullable ThrowableError getCause() {
        final @Nullable Throwable cause = throwable.getCause();
        if(cause != null) {
            return new ThrowableError(cause);
        } else {
            return null;
        }
    }

    @Override public @Nullable ListView<StackTraceElement> getStackTrace() {
        // TODO: create an ArrayView to prevent copying the stacktrace?
        return ListView.of(throwable.getStackTrace());
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ThrowableError that = (ThrowableError)o;
        return throwable.equals(that.throwable);
    }

    @Override public int hashCode() {
        return throwable.hashCode();
    }

    @Override public String toString() {
        return throwable.toString();
    }
}
