package mb.tiger;

import mb.esv.common.ESVStylingRules;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class StylingRules implements Serializable {
    final ESVStylingRules stylingRules;

    private StylingRules(ESVStylingRules stylingRules) {
        this.stylingRules = stylingRules;
    }

    public static StylingRules fromClassLoaderResources() throws IOException {
        final String resource = "target/metaborg/editor.esv.af";
        try(final @Nullable InputStream inputStream = ParseTable.class.getClassLoader().getResourceAsStream(resource)) {
            if(inputStream == null) {
                throw new RuntimeException(
                    "Cannot create styling rules; cannot find resource '" + resource + "' in classloader resources");
            }
            final ESVStylingRules stylingRules = ESVStylingRules.fromStream(inputStream);
            return new StylingRules(stylingRules);
        }
    }
}
