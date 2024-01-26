package mb.spoofax.cli;

import org.checkerframework.checker.nullness.qual.Nullable;

public class SpoofaxCliException extends Exception {

    public SpoofaxCliException(String message) {
        super(message);
    }

    public SpoofaxCliException(String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
