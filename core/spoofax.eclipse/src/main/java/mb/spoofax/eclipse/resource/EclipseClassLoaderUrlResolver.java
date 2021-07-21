package mb.spoofax.eclipse.resource;

import mb.resource.classloader.ClassLoaderUrlResolver;
import mb.resource.util.UriEncode;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.runtime.FileLocator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class EclipseClassLoaderUrlResolver implements ClassLoaderUrlResolver {
    @Override public @Nullable URL resolve(URL url) {
        try {
            final URL resolvedUrl = FileLocator.resolve(url);
            if(url.equals(resolvedUrl)) return null; // Returns same URL -> URL was not resolved -> return null.

            // `ClassLoaderUrlResolver` instances are meant to return encoded URLs, but
            // Eclipse has an outstanding bug from 2006 that makes it return unencoded
            // values (https://bugs.eclipse.org/bugs/show_bug.cgi?id=145096). Explicitly
            // encode them here.
            return UriEncode.encodeToUri(resolvedUrl.toString()).toURL();
        } catch(IOException | URISyntaxException e) {
            // Resolution failed -> return null.
            return null;
        }
    }
}
