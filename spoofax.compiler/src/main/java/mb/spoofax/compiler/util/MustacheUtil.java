package mb.spoofax.compiler.util;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MustacheUtil {
    public final static Mustache.Compiler compiler = Mustache.compiler().escapeHTML(false); // Compiler is thread-safe, so static is fine.

    public static Template compile(Class<?> clazz, String resource) {
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
