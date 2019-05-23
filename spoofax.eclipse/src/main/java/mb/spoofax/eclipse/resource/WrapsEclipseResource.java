package mb.spoofax.eclipse.resource;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IResource;

public interface WrapsEclipseResource {
    @Nullable IResource getWrappedEclipseResource();
}
