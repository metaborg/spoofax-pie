package mb.spoofax.core.pie;

import mb.pie.api.Pie;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Provider for Pie instances.
 */
public interface PieProvider {
    /**
     * Creates a Pie instance for use in a particular project.
     * @param projectDir The project to create a Pie instance for. If not provided, a default should be returned.
     * @return A Pie instance.
     */
    Pie getPie(@Nullable ResourcePath projectDir);
}
