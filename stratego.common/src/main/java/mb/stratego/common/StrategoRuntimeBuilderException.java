package mb.stratego.common;

import org.checkerframework.checker.nullness.qual.Nullable;

public class StrategoRuntimeBuilderException extends Exception {
    public StrategoRuntimeBuilderException() {
        super();
    }

    public StrategoRuntimeBuilderException(String message) {
        super(message);
    }

    public StrategoRuntimeBuilderException(String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public StrategoRuntimeBuilderException(Throwable cause) {
        super(cause);
    }
}
