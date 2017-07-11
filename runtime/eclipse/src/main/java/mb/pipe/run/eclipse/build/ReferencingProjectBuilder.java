package mb.pipe.run.eclipse.build;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import mb.pipe.run.core.log.Logger;

public class ReferencingProjectBuilder extends WorkspaceJob {
    private final IProject project;
    private final Logger logger;


    public ReferencingProjectBuilder(IProject project, Logger logger) {
        super("Triggering referencing projects");
        this.project = project;
        this.logger = logger;
    }


    @Override public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
        for(IProject referencingProject : project.getReferencingProjects()) {
            logger.info("Triggering build for referencing project {}", referencingProject);
            referencingProject.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
            // referencingProject.build(IncrementalProjectBuilder.FULL_BUILD, PipeProjectBuilder.id,
            // new HashMap<String, String>(), monitor);
        }
        return Status.OK_STATUS;
    }
}
