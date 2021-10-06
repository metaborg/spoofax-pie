package mb.spoofax.eclipse.resource;

import mb.resource.ReadableResource;
import mb.resource.classloader.ClassLoaderToNativeResolver;
import mb.resource.classloader.ClassLoaderUrlResolver;
import mb.resource.util.UriEncode;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.runtime.FileLocator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class EclipseClassLoaderToNativeResolver implements ClassLoaderToNativeResolver {
    @Override public @Nullable ReadableResource toNativeResource(URL url) {
        return null; // TODO: implement.
    }
}
