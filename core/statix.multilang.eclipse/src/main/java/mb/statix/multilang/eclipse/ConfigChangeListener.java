package mb.statix.multilang.eclipse;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public interface ConfigChangeListener {
    void configChanged(IProject project, @Nullable IProgressMonitor monitor);
}
