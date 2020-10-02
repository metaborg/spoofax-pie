package mb.spoofax.compiler.util;

import com.samskivert.mustache.CachingMustacheCompiler;
import com.samskivert.mustache.Template;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TemplateCompiler {
    private final static CachingMustacheCompiler sharedCompiler = CachingMustacheCompiler.cachingCompiler();
    private final Class<?> clazz;
    private final Charset charset;

    TemplateCompiler(Class<?> clazz, Charset charset) {
        this.clazz = clazz;
        this.charset = charset;
    }

    public TemplateCompiler(Charset charset) {
        this(TemplateCompiler.class, charset);
    }

    public TemplateCompiler loadingFromClass(Class<?> clazz) {
        return new TemplateCompiler(clazz, charset);
    }

    public Template getOrCompile(String resource) {
        final CachingMustacheCompiler compiler = sharedCompiler.withLoader(this::reader);
        return compiler.loadTemplate(resource);
    }

    public TemplateWriter getOrCompileToWriter(String resource) {
        final CachingMustacheCompiler compiler = sharedCompiler.withLoader(this::reader);
        final Template template = compiler.loadTemplate(resource);
        return new TemplateWriter(template, charset);
    }

    private Reader reader(String resource) {
        final @Nullable InputStream inputStream = clazz.getResourceAsStream(resource);
        if(inputStream == null) {
            throw new RuntimeException("Cannot create reader; cannot find '" + resource + "' in resources of '" + clazz + "'");
        }
        return new InputStreamReader(inputStream, StandardCharsets.UTF_8);
    }
}
