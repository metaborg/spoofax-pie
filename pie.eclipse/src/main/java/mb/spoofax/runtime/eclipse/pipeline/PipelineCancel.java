package mb.spoofax.runtime.eclipse.pipeline;

import org.eclipse.core.runtime.IProgressMonitor;

import mb.spoofax.runtime.eclipse.util.Nullable;
import mb.util.async.Cancelled;
import mb.util.async.NullCancelled;

public class PipelineCancel {
    public static Cancelled cancelled(@Nullable IProgressMonitor monitor) {
        if(monitor != null) {
            return new MonitorCancelToken(monitor);
        } else {
            return new NullCancelled();
        }
    }
}
