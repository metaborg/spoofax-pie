package mb.spoofax.runtime.eclipse.pipeline;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import mb.util.async.Cancel;
import mb.util.async.Cancelled;

public class MonitorCancelToken implements Cancelled, Cancel {
    public SubMonitor monitor;

    public MonitorCancelToken(IProgressMonitor monitor) {
        this.monitor = SubMonitor.convert(monitor);
    }

    public MonitorCancelToken(SubMonitor monitor) {
        this.monitor = monitor;
    }

    @Override public void cancel() {
        monitor.setCanceled(true);
    }

    @Override public boolean cancelled() {
        return monitor.isCanceled();
    }

    @Override public void throwIfCancelled() throws InterruptedException {
        if(monitor.isCanceled()) {
            throw new InterruptedException();
        }
    }
}
