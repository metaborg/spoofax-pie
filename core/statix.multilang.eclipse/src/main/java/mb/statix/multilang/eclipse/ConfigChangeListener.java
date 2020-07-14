package mb.statix.multilang.eclipse;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Interface used by {@link MultiLangPlugin} to notify languages when the multilang.yaml config file is changed.
 */
public interface ConfigChangeListener {
    void configChanged(IProject project, @Nullable IProgressMonitor monitor);
}
