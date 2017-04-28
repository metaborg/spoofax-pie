package mb.pipe.run.core;


public class PipeEx extends Exception {
    private static final long serialVersionUID = -3661402088434126639L;


    public PipeEx() {
        super();
    }

    public PipeEx(String message) {
        super(message);
    }

    public PipeEx(String message, Throwable cause) {
        super(message, cause);
    }

    public PipeEx(Throwable cause) {
        super(cause);
    }
}
