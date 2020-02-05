package mb.spoofax.eclipse.job;

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
    private final Logger logger;
    private final Thread thread;
    private final long killTimeMillis;

    public ThreadKillerJob(LoggerFactory loggerFactory, Thread thread, long killTimeMs) {
        super("Killing thread");

        this.logger = loggerFactory.create(getClass());
        this.thread = thread;
        this.killTimeMillis = killTimeMs;

        setSystem(true);
        setPriority(INTERACTIVE);
    }

    @SuppressWarnings("deprecation") @Override protected IStatus run(IProgressMonitor monitor) {
        if(monitor.isCanceled()) return StatusUtil.cancel();

        logger.warn("Interrupting {}, killing after {}ms", thread, killTimeMillis);
        thread.interrupt();

        try {
            Thread.sleep(killTimeMillis);
        } catch(InterruptedException e) {
            return StatusUtil.cancel();
        }

        if(monitor.isCanceled()) return StatusUtil.cancel();
        logger.warn("Killing {}", thread);
        thread.stop();
        return StatusUtil.success();
    }
}
