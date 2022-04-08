package mb.spoofax.core.language;

public class NoResourceExportsException extends RuntimeException {
    public NoResourceExportsException(String message) {
        super(message);
    }

    public NoResourceExportsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoResourceExportsException(Throwable cause) {
        super(cause);
    }

    public NoResourceExportsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
