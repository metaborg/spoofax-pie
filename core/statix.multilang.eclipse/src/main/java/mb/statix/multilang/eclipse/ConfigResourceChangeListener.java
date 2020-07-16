package mb.statix.multilang.eclipse;

import mb.log.api.Logger;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.util.StatusUtil;
import mb.statix.multilang.MultiLangAnalysisException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link IResourceChangeListener} that listens to changes to {@code multilang.yaml} files, and notifies all registered
 * {@link ConfigChangeListener config change listeners}.
 */
public class ConfigResourceChangeListener implements IResourceChangeListener {
    private static final Logger logger = SpoofaxPlugin.getComponent().getLoggerFactory().create(ConfigResourceChangeListener.class);
    private final ArrayList<ConfigChangeListener> delegates = new ArrayList<>();

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        IResourceDelta rootDelta = event.getDelta();
        ArrayList<IProject> projects = new ArrayList<>();

        try {
            rootDelta.accept(delta -> {
                if(delta.getResource().getType() == IResource.ROOT) {
                    return true;
                }
                if(delta.getResource().getType() == IResource.PROJECT) {
                    for(IResourceDelta child : delta.getAffectedChildren()) {
                        // Only trigger when a multilang.yaml file in project root in changed
                        if(child.getResource().getType() == IResource.FILE && child.getResource().getName().equals("multilang.yaml")) {
                            projects.add((IProject)delta.getResource());
                        }
                    }
                    return true; // For nested projects
                }
                return false;
            });
        } catch(CoreException e) {
            logger.error("Error when updating analysis after config change", e);
        }

        new WorkspaceJob("Process multilang.yaml change") {
            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) {
                List<Throwable> exceptions = new ArrayList<>();
                // Update all projects for changes configs
                projects.forEach(path -> delegates.forEach(delegate -> SafeRunner.run(new SafeRunnable() {
                    @Override public void run() {
                        delegate.configChanged(path, monitor);
                    }

                    @Override
                    public void handleException(Throwable e) {
                        exceptions.add(e);
                    }
                })));
                if(exceptions.isEmpty()) {
                    return StatusUtil.success();
                }
                MultiLangAnalysisException wrapper = new MultiLangAnalysisException("Errors processing multilang.yaml change", false);
                exceptions.forEach(wrapper::addSuppressed);
                return StatusUtil.error("Errors processing multilang.yaml change", wrapper);
            }
        }.schedule();
    }

    public void addDelegate(ConfigChangeListener configChangeListener) {
        delegates.add(configChangeListener);
    }

    public void removeDelegate(ConfigChangeListener configChangeListener) {
        delegates.remove(configChangeListener);
    }

    public void clearDelegates() {
        delegates.clear();
    }
}
