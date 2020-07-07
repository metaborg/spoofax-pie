package mb.common.result;

import org.checkerframework.checker.nullness.qual.Nullable;

public class ExpectException extends RuntimeException {
    public ExpectException(String message) {
        super(message, null, true, false);
    }

    public ExpectException(String message, @Nullable Throwable cause) {
        super(message, cause, true, false);
    }
}
