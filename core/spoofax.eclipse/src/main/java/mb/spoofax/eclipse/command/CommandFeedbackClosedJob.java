package mb.spoofax.eclipse.command;

import mb.pie.api.MixedSession;
import mb.pie.api.TaskKey;
import mb.pie.dagger.PieComponent;
import mb.spoofax.eclipse.util.StatusUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

public class CommandFeedbackClosedJob extends Job {
    private final PieComponent pieComponent;
    private final TaskKey key;

    public CommandFeedbackClosedJob(
        PieComponent pieComponent,
        TaskKey key
    ) {
        super("Command feedback editor close");
        this.pieComponent = pieComponent;
        this.key = key;
    }

    @Override protected IStatus run(@NonNull IProgressMonitor monitor) {
        try(final MixedSession session = pieComponent.newSession()) {
            session.unobserve(key);
            session.removeCallback(key);
        }
        return StatusUtil.success();
    }
}
