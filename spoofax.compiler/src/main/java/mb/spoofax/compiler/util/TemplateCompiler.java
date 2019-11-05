package mb.spoofax.compiler.util;

import com.samskivert.mustache.DefaultCollector;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

public class TemplateCompiler {
    private final static /* Compiler is thread-safe, so static is fine. */ Mustache.Compiler compiler = Mustache.compiler()
        .escapeHTML(false)
        .withCollector(new DefaultCollector() {
            public Iterator<?> toIterator(final Object value) {
                if(value instanceof Optional<?>) { // Support Optional values that are not present.
                    Optional<?> opt = (Optional<?>)value;
                    return opt.isPresent() ? Collections.singleton(opt.get()).iterator() : Collections.emptyIterator();
                } else return super.toIterator(value);
            }
        });

    private final Class<?> clazz;

    public TemplateCompiler(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Template compile(String resource) {
        try {
            try(final @Nullable InputStream inputStream = clazz.getResourceAsStream(resource)) {
                if(inputStream == null) {
                    throw new RuntimeException("Cannot compile Mustache template; cannot find '" + resource + "' in classloader resources");
                }
                return compiler.compile(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            }
        } catch(IOException e) {
            throw new RuntimeException("Cannot compile Mustache template; reading '" + resource + "' from classloader resources failed unexpectedly", e);
        }
    }
}
