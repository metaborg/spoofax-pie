package mb.common.util;

/**
 * Wraps a {@link Exception} into an unchecked exception. Used to wrap exceptions in lambda's to rethrow them later.
 */
public class UncheckedException extends RuntimeException {
    public UncheckedException(Exception cause) {
        super(cause);
    }

    @Override public Exception getCause() {
        return (Exception)super.getCause();
    }


    @Override public Throwable fillInStackTrace() {
        return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
    }
}
