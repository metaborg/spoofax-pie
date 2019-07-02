package mb.spoofax.eclipse.nature;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public abstract class SpoofaxProjectNature implements IProjectNature {
    @SuppressWarnings("ConstantConditions") private @MonotonicNonNull IProject project = null;


    @Override public abstract void configure() throws CoreException;

    @Override public abstract void deconfigure() throws CoreException;


    @Override public IProject getProject() {
        return project;
    }

    @Override public void setProject(@NonNull IProject project) {
        this.project = project;
    }
}
