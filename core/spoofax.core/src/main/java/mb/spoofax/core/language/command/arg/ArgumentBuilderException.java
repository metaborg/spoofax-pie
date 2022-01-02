package mb.spoofax.core.language.command.arg;

public class ArgumentBuilderException extends RuntimeException {
    public ArgumentBuilderException() {
    }

    public ArgumentBuilderException(String message) {
        super(message);
    }

    public ArgumentBuilderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArgumentBuilderException(Throwable cause) {
        super(cause);
    }
}
