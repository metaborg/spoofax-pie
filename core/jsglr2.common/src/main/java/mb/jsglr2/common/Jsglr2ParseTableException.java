package mb.jsglr2.common;

public class Jsglr2ParseTableException extends Exception {
    public Jsglr2ParseTableException() {

    }

    public Jsglr2ParseTableException(String message) {
        super(message);
    }

    public Jsglr2ParseTableException(String message, Throwable cause) {
        super(message, cause);
    }

    public Jsglr2ParseTableException(Throwable cause) {
        super(cause);
    }

    public Jsglr2ParseTableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
