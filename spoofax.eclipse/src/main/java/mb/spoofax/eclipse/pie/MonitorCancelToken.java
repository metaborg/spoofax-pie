package mb.spoofax.eclipse.pie;

import mb.pie.api.exec.CancelToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;


/**
 * A cancellation token wrapping an Eclipse {@link IProgressMonitor}.
 */
public final class MonitorCancelToken implements CancelToken {

    private final SubMonitor monitor;

    /**
     * Initializes a new instance of the {@link MonitorCancelToken} class.
     *
     * @param monitor The progress monitor to monitor; or {@code null}.
     */
    public MonitorCancelToken(@Nullable IProgressMonitor monitor) {
        this.monitor = SubMonitor.convert(monitor);
    }

    /**
     * Initializes a new instance of the {@link MonitorCancelToken} class.
     *
     * @param monitor The sub monitor to monitor.
     */
    public MonitorCancelToken(SubMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public boolean isCanceled() {
        return this.monitor.isCanceled();
    }

    /**
     * Gets the progress monitor from this token.
     *
     * @return The progress monitor.
     */
    public SubMonitor getMonitor() { return this.monitor; }
}
