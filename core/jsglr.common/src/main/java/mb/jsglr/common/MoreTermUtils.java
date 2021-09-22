package mb.jsglr.common;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.io.binary.TermReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@SuppressWarnings("unused")
public final class MoreTermUtils {

    private MoreTermUtils() {}

    /**
     * Loads a term from class loader resources,
     * using the default imploder term factory.
     *
     * @param cls the class
     * @param resource the resource path, which should start with a slash
     * @return the read term
     */
    public static IStrategoTerm fromClassLoaderResources(Class<?> cls, String resource) throws IOException {
        final ITermFactory termFactory = new ImploderOriginTermFactory(new TermFactory());
        return fromClassLoaderResources(cls, resource, termFactory);
    }

    /**
     * Loads a term from class loader resources,
     * using the specified term factory.
     *
     * @param cls the class
     * @param resourcePath the resource path
     * @param termFactory the term factory
     * @return the read term
     */
    public static IStrategoTerm fromClassLoaderResources(Class<?> cls, String resourcePath, ITermFactory termFactory) throws IOException {
        @Nullable URL resource = cls.getResource(resourcePath);
        if (resource == null) {
            @Nullable URL root = cls.getResource("/");
            throw new RuntimeException("Cannot find ATerm resource '" + resourcePath + "' in classloader resources relative to " + (root != null ? root.getPath() : "<unknown>") );
        }
        try(final InputStream inputStream = resource.openStream()) {
            return fromStream(inputStream, termFactory);
        }
    }

    /**
     * Loads a term from a stream.
     *
     * @param stream the stream to read from
     * @return the read term
     */
    public static IStrategoTerm fromStream(InputStream stream) throws IOException {
        final ITermFactory termFactory = new ImploderOriginTermFactory(new TermFactory());
        return fromStream(stream, termFactory);
    }

    /**
     * Loads a term from a stream.
     *
     * @param stream the stream to read from
     * @param termFactory the term factory
     * @return the read term
     */
    public static IStrategoTerm fromStream(InputStream stream, ITermFactory termFactory) throws IOException {
        final TermReader reader = new TermReader(termFactory);
        return reader.parseFromStream(stream);
    }

}
