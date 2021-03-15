package mb.spoofax.eclipse.resource;

import mb.resource.classloader.ClassLoaderUrlResolver;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.runtime.FileLocator;

import java.io.IOException;
import java.net.URL;

public class EclipseClassLoaderUrlResolver implements ClassLoaderUrlResolver {
    @Override public @Nullable URL resolve(URL url) {
        try {
            final URL resolvedUrl = FileLocator.resolve(url);
            if(url.equals(resolvedUrl)) return null; // Returns same URL -> URL was not resolved -> return null.
            return resolvedUrl;
        } catch(IOException e) {
            // Resolution failed -> return null.
            return null;
        }
    }
}
