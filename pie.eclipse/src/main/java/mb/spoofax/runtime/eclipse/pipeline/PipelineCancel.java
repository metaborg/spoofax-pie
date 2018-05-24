package mb.spoofax.runtime.eclipse.pipeline;

import org.eclipse.core.runtime.IProgressMonitor;

import mb.pie.api.exec.Cancelled;
import mb.pie.api.exec.NullCancelled;
import mb.spoofax.runtime.eclipse.util.Nullable;

public class PipelineCancel {
    public static Cancelled cancelled(@Nullable IProgressMonitor monitor) {
        if(monitor != null) {
            return new MonitorCancelToken(monitor);
        } else {
            return new NullCancelled();
        }
    }
}
