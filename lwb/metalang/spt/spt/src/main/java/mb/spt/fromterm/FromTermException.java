package mb.spt.fromterm;

public class FromTermException extends RuntimeException {
    public FromTermException() {}

    public FromTermException(String message) {
        super(message);
    }

    public FromTermException(String message, Throwable cause) {
        super(message, cause);
    }

    public FromTermException(Throwable cause) {
        super(cause);
    }

    public FromTermException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
