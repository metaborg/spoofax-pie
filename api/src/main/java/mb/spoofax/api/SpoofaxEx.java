package mb.spoofax.api;


public class SpoofaxEx extends Exception {
    private static final long serialVersionUID = -3661402088434126639L;


    public SpoofaxEx() {
        super();
    }

    public SpoofaxEx(String message) {
        super(message);
    }

    public SpoofaxEx(String message, Throwable cause) {
        super(message, cause);
    }

    public SpoofaxEx(Throwable cause) {
        super(cause);
    }
}
