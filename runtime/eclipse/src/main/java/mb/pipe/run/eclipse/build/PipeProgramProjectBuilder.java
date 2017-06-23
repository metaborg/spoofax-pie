package mb.pipe.run.eclipse.build;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import mb.pipe.run.eclipse.PipePlugin;

public class PipeProgramProjectBuilder extends IncrementalProjectBuilder {
    public static final String id = PipePlugin.id + ".builder.program";

    @Override protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
        throws CoreException {
        return null;
    }
}
