package mb.pipe.run.core;

public class PipeRunEx extends RuntimeException {
    private static final long serialVersionUID = -7668398660885536315L;


    public PipeRunEx() {
        super();
    }

    public PipeRunEx(String message) {
        super(message);
    }

    public PipeRunEx(String message, Throwable cause) {
        super(message, cause);
    }

    public PipeRunEx(Throwable cause) {
        super(cause);
    }
}
