package mb.spoofax.eclipse.pie;

import mb.pie.api.exec.Cancelable;
import mb.pie.api.exec.CancelToken;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

/**
 * A cancellation token wrapping an Eclipse {@link IProgressMonitor}.
 */
public final class MonitorCancelableToken implements CancelToken, Cancelable {

    public SubMonitor monitor;

    /**
     * Initializes a new instance of the {@link MonitorCancelableToken} class.
     *
     * @param monitor The progress monitor to monitor; or {@code null}.
     */
    public MonitorCancelableToken(IProgressMonitor monitor) {
        this.monitor = SubMonitor.convert(monitor);
    }

    /**
     * Initializes a new instance of the {@link MonitorCancelableToken} class.
     *
     * @param monitor The sub monitor to monitor.
     */
    public MonitorCancelableToken(SubMonitor monitor) {
        this.monitor = monitor;
    }

    @Override public void requestCancel() {
        monitor.setCanceled(true);
    }

    @Override public boolean isCanceled() {
        return monitor.isCanceled();
    }

}
