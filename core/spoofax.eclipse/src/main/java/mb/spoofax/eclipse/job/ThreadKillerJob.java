package mb.spoofax.eclipse.job;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.spoofax.eclipse.util.StatusUtil;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Job that interrupts given thread when scheduled, and kills the thread after a certain time.
 */
public class ThreadKillerJob extends Job {
    @AssistedFactory public interface Factory {
        ThreadKillerJob create(
            Thread thread,
            long killTimeMs
        );
    }

    private final Logger logger;
    private final Thread thread;
    private final long killTimeMs;

    @AssistedInject public ThreadKillerJob(
        LoggerFactory loggerFactory,
        @Assisted Thread thread,
        @Assisted long killTimeMs
    ) {
        super("Killing thread " + thread);

        this.logger = loggerFactory.create(getClass());
        this.thread = thread;
        this.killTimeMs = killTimeMs;

        setSystem(true);
        setPriority(INTERACTIVE);
    }

    @SuppressWarnings("deprecation") @Override protected IStatus run(IProgressMonitor monitor) {
        if(monitor.isCanceled()) return StatusUtil.cancel();

        try {
            Thread.sleep(killTimeMs);
        } catch(InterruptedException e) {
            return StatusUtil.cancel();
        }

        if(monitor.isCanceled()) return StatusUtil.cancel();
        logger.warn("Killing thread {}", thread);
        thread.stop();
        return StatusUtil.success();
    }
}
