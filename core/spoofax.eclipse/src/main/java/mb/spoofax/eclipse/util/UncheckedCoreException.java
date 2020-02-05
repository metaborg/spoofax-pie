package mb.spoofax.eclipse.util;

import org.eclipse.core.runtime.CoreException;

public class UncheckedCoreException extends RuntimeException {
    public UncheckedCoreException() {
        super();
    }

    public UncheckedCoreException(String message) {
        super(message);
    }

    public UncheckedCoreException(String message, CoreException cause) {
        super(message, cause);
    }

    public UncheckedCoreException(CoreException cause) {
        super(cause);
    }

    @Override public synchronized CoreException getCause() {
        return (CoreException) super.getCause();
    }
}
