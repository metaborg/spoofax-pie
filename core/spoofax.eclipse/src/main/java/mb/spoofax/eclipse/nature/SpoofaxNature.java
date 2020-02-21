package mb.spoofax.eclipse.nature;

import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.util.BuilderUtil;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public abstract class SpoofaxNature implements IProjectNature {
    private final EclipseLanguageComponent languageComponent;

    @SuppressWarnings("ConstantConditions") private @MonotonicNonNull IProject project = null;


    protected SpoofaxNature(EclipseLanguageComponent languageComponent) {
        this.languageComponent = languageComponent;
    }


    @Override public void configure() throws CoreException {
        BuilderUtil.append(languageComponent.getEclipseIdentifiers().getProjectBuilder(), getProject(), null);
    }

    @Override public void deconfigure() throws CoreException {
        BuilderUtil.removeFrom(languageComponent.getEclipseIdentifiers().getProjectBuilder(), getProject(), null);
    }


    @Override public IProject getProject() {
        return project;
    }

    @Override public void setProject(@NonNull IProject project) {
        this.project = project;
    }
}
