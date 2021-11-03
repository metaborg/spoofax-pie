package mb.spoofax.eclipse.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

public class SerializableCoreException extends Exception {
    public SerializableCoreException(CoreException e) {
        super(e.getStatus().getMessage(), e.getStatus().getException());
        setStackTrace(e.getStackTrace()); // Copy stacktrace
        for(IStatus child : e.getStatus().getChildren()) {
            addSuppressed(new SerializableCoreException(child));
        }
    }

    public SerializableCoreException(IStatus status) {
        super(status.getMessage(), status.getException());
        for(IStatus child : status.getChildren()) {
            addSuppressed(new SerializableCoreException(child));
        }
    }

    @Override public synchronized Throwable fillInStackTrace() {
        // Do nothing so no stack trace is filled in on creation. It is either set with setStackTrace, or not set all
        // when this exception is created from an IStatus object. In that case, a stacktrace can come from the
        // exception of the IStatus object (if it has one).
        return this;
    }
}
