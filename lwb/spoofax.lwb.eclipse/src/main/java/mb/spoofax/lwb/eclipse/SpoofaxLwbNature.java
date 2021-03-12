package mb.spoofax.lwb.eclipse;

import mb.spoofax.eclipse.util.AbstractHandlerUtil;
import mb.spoofax.eclipse.util.NatureUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class SpoofaxLwbNature implements IProjectNature {
    public static final String id = SpoofaxLwbPlugin.id + ".nature";

    private @Nullable IProject project = null;


    @Override public void configure() throws CoreException {
        //BuilderUtil.append(languageComponent.getEclipseIdentifiers().getProjectBuilder(), getProject(), null);
    }

    @Override public void deconfigure() throws CoreException {
        //BuilderUtil.removeFrom(languageComponent.getEclipseIdentifiers().getProjectBuilder(), getProject(), null);
    }

    @Override public @Nullable IProject getProject() {
        return project;
    }

    @Override public void setProject(@NonNull IProject project) {
        this.project = project;
    }


    public static boolean hasNature(IProject project) throws CoreException {
        return NatureUtil.exists(id, project);
    }

    public static void addTo(IProject project, @Nullable IProgressMonitor monitor) throws CoreException {
        NatureUtil.addTo(id, project, monitor);
    }

    public static void removeFrom(IProject project, @Nullable IProgressMonitor monitor) throws CoreException {
        NatureUtil.removeFrom(id, project, monitor);
    }


    public static class AddHandler extends AbstractHandler {
        public static final String commandId = id + ".add";

        @Override public @Nullable Object execute(@NonNull ExecutionEvent event) throws ExecutionException {
            final @Nullable IProject project = AbstractHandlerUtil.toProject(event);
            if(project == null) return null;
            try {
                addTo(project, null);
            } catch(CoreException e) {
                throw new ExecutionException("Adding " + id + " nature failed unexpectedly", e);
            }
            return null;
        }
    }

    public static class RemoveHandler extends AbstractHandler {
        public static final String commandId = id + ".remove";

        @Override public @Nullable Object execute(@NonNull ExecutionEvent event) throws ExecutionException {
            final @Nullable IProject project = AbstractHandlerUtil.toProject(event);
            if(project == null) return null;
            try {
                removeFrom(project, null);
            } catch(CoreException e) {
                throw new ExecutionException("Removing " + id + " nature failed unexpectedly", e);
            }
            return null;
        }
    }

}
