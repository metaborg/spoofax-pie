package mb.stratego.common;

import org.checkerframework.checker.nullness.qual.Nullable;

public class StrategoException extends Exception {
    public StrategoException() {
        super();
    }

    public StrategoException(String message) {
        super(message);
    }

    public StrategoException(String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public StrategoException(Throwable cause) {
        super(cause);
    }
}
