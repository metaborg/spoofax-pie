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
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import java.util.ArrayList;

public class ConfigResourceChangeListener implements IResourceChangeListener {
    private static final Logger logger = SpoofaxPlugin.getComponent().getLoggerFactory().create(ConfigResourceChangeListener.class);
    private final ArrayList<ConfigChangeListener> delegates = new ArrayList<>();

    public ConfigResourceChangeListener() {
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        IResourceDelta rootDelta = event.getDelta();
        ArrayList<IProject> projects = new ArrayList<>();

        try {
            rootDelta.accept(delta -> {
                if(delta.getResource().getType() == IResource.ROOT) {
                    // If change is project, visit children (possibly multilang.yaml)
                    return true;
                }
                if(delta.getResource().getType() == IResource.PROJECT) {
                    for(IResourceDelta child : delta.getAffectedChildren()) {
                        if(child.getResource().getType() == IResource.FILE && child.getResource().getName().equals("multilang.yaml")) {
                            projects.add((IProject)delta.getResource());
                        }
                    }
                    return true; // For nested projects
                }
                return false;
            });
        } catch(CoreException e) {
            throw new MultiLangAnalysisException("Error when updating analysis after config change", e);
        }

        new WorkspaceJob("Process multilang.yaml change") {
            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) {
                // Update all projects for changes configs
                projects.forEach(path -> delegates.forEach(delegate -> delegate.configChanged(path, monitor)));
                return StatusUtil.success();
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
