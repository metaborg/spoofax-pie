package mb.spoofax.eclipse.resource;

import mb.resource.ReadableResource;
import mb.resource.ResourceRuntimeException;
import mb.resource.classloader.ClassLoaderToNativeResolver;
import mb.resource.dagger.ResourceServiceScope;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@ResourceServiceScope
public class EclipseClassLoaderToNativeResolver implements ClassLoaderToNativeResolver {
    private final EclipseResourceRegistry resourceRegistry;

    @Inject public EclipseClassLoaderToNativeResolver(EclipseResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override public @Nullable ReadableResource toNativeResource(URL url) {
        try {
            final URI uri = url.toURI();
            return null; // TODO: implement
        } catch(URISyntaxException e) {
            throw new ResourceRuntimeException("Could not convert URL '" + url + "' to an URI", e);
        }
    }
}
