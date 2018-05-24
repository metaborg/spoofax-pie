package mb.spoofax.runtime.eclipse.pipeline;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import com.google.inject.Inject;

import mb.log.Logger;
import mb.spoofax.runtime.eclipse.nature.SpoofaxNature;
import mb.spoofax.runtime.eclipse.util.NatureUtils;

public class PipelineProjectManager implements IResourceChangeListener {
    private final Logger logger;
    private final PipelineAdapter pipelineAdapter;


    @Inject public PipelineProjectManager(Logger logger, PipelineAdapter pipelineAdapter) {
        this.logger = logger.forContext(getClass());
        this.pipelineAdapter = pipelineAdapter;
    }


    public void initialize() throws CoreException {
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();

        // Register existing projects
        for(IProject project : workspace.getRoot().getProjects()) {
            if(project.isAccessible() && NatureUtils.exists(SpoofaxNature.id, project)) {
                pipelineAdapter.addProject(project);
            }
        }

        // Register resource change listener
        workspace.addResourceChangeListener(this,
            IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.POST_CHANGE);
    }


    @Override public void resourceChanged(IResourceChangeEvent event) {
        try {
            switch(event.getType()) {
                case IResourceChangeEvent.PRE_CLOSE: {
                    final IProject project = (IProject) event.getResource();
                    if(NatureUtils.exists(SpoofaxNature.id, project)) {
                        logger.debug("Spoofax project {} was closed", project);
                        pipelineAdapter.removeProject(project);
                    }
                    break;
                }
                case IResourceChangeEvent.PRE_DELETE: {
                    final IProject project = (IProject) event.getResource();
                    if(NatureUtils.exists(SpoofaxNature.id, project)) {
                        logger.debug("Spoofax project {} was deleted", project);
                        pipelineAdapter.removeProject(project);
                    }
                    break;
                }
                case IResourceChangeEvent.POST_CHANGE:
                    event.getDelta().accept(delta -> {
                        // Only match Spoofax projects.
                        final IResource resource = delta.getResource();
                        if(resource.getType() != IResource.PROJECT) {
                            return true; // Recurse into projects of workspace.
                        }
                        final IProject project = (IProject) resource;
                        if(project.isAccessible() && !NatureUtils.exists(SpoofaxNature.id, project)) {
                            return false; // Never recurse into projects, since projects cannot be nested.
                        }

                        // Determine kind of change.
                        switch(delta.getKind()) {
                            case IResourceDelta.ADDED: {
                                logger.debug("Spoofax project {} was added", project);
                                if(project.isAccessible()) {
                                    pipelineAdapter.addProject(project);
                                }
                                break;
                            }
                            case IResourceDelta.CHANGED: {
                                if((delta.getFlags() & IResourceDelta.OPEN) != 0) {
                                    if(project.isAccessible()) { // Project was opened
                                        logger.debug("Spoofax project {} was opened", project);
                                        pipelineAdapter.addProject(project);
                                    }
                                }
                                break;
                            }
                        }
                        return false; // Never recurse into projects, since projects cannot be nested.
                    });
            }
        } catch(CoreException e) {
            logger.error("Failed to process resource delta", e);
        }
    }
}
