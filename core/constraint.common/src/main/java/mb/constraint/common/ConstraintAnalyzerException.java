package mb.constraint.common;

import org.checkerframework.checker.nullness.qual.Nullable;

public class ConstraintAnalyzerException extends Exception {
    public ConstraintAnalyzerException() {
        super();
    }

    public ConstraintAnalyzerException(String message) {
        super(message);
    }

    public ConstraintAnalyzerException(String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public ConstraintAnalyzerException(Throwable cause) {
        super(cause);
    }
}
