package mb.spoofax.compiler.util;

import com.samskivert.mustache.CachingMustacheCompiler;
import com.samskivert.mustache.Template;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class TemplateCompiler {
    private final static CachingMustacheCompiler sharedCompiler = CachingMustacheCompiler.cachingCompiler();
    private final Class<?> clazz;

    public TemplateCompiler(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Template getOrCompile(String resource) {
        final CachingMustacheCompiler compiler = sharedCompiler.withLoader(this::reader);
        return compiler.loadTemplate(resource);
    }

    private Reader reader(String resource) {
        final @Nullable InputStream inputStream = clazz.getResourceAsStream(resource);
        if(inputStream == null) {
            throw new RuntimeException("Cannot create reader; cannot find '" + resource + "' in classloader resources");
        }
        return new InputStreamReader(inputStream, StandardCharsets.UTF_8);
    }
}
