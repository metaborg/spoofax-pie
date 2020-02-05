package mb.jsglr1.common;

public class JSGLR1ParseTableException extends Exception {
    public JSGLR1ParseTableException() {

    }

    public JSGLR1ParseTableException(String message) {
        super(message);
    }

    public JSGLR1ParseTableException(String message, Throwable cause) {
        super(message, cause);
    }

    public JSGLR1ParseTableException(Throwable cause) {
        super(cause);
    }

    public JSGLR1ParseTableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
