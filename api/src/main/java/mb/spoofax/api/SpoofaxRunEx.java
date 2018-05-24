package mb.spoofax.api;

public class SpoofaxRunEx extends RuntimeException {
    private static final long serialVersionUID = -7668398660885536315L;


    public SpoofaxRunEx() {
        super();
    }

    public SpoofaxRunEx(String message) {
        super(message);
    }

    public SpoofaxRunEx(String message, Throwable cause) {
        super(message, cause);
    }

    public SpoofaxRunEx(Throwable cause) {
        super(cause);
    }
}
