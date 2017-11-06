package mb.spoofax.runtime.eclipse.build;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import com.google.inject.Injector;

import mb.log.Logger;
import mb.pie.runtime.core.ExecException;
import mb.spoofax.runtime.eclipse.SpoofaxPlugin;
import mb.spoofax.runtime.eclipse.pipeline.PipelineAdapter;
import mb.spoofax.runtime.eclipse.util.StatusUtils;

public class SpoofaxProjectBuilder extends IncrementalProjectBuilder {
    public static final String id = SpoofaxPlugin.id + ".builder";

    private final Logger logger;
    private final PipelineAdapter pipelineAdapter;


    public SpoofaxProjectBuilder() {
        final Injector injector = SpoofaxPlugin.spoofaxFacade().injector;
        this.logger = injector.getInstance(Logger.class).forContext(getClass());
        this.pipelineAdapter = injector.getInstance(PipelineAdapter.class);
    }


    @Override protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
        throws CoreException {
        final IProject currentEclipseProject = getProject();
        logger.info("Building project {}", currentEclipseProject);

        final IResourceDelta delta = getDelta(currentEclipseProject);
        if(delta != null) {
            pipelineAdapter.pathsChanged(delta);
        }

        try {
            pipelineAdapter.executeAll(monitor);

        } catch(@SuppressWarnings("unused") InterruptedException e) {
            logger.debug("Pipeline execution cancelled");
            rememberLastBuiltState();
        } catch(ExecException e) {
            logger.error("Pipeline execution failed", e);
            throw new CoreException(StatusUtils.buildFailure("Pipeline execution failed", e));
        }

        return null;
    }

    @Override protected void clean(IProgressMonitor monitor) throws CoreException {
        pipelineAdapter.cleanAll();
        forgetLastBuiltState();
    }

    @Override public ISchedulingRule getRule(int kind, Map<String, String> args) {
        return null;
    }
}
