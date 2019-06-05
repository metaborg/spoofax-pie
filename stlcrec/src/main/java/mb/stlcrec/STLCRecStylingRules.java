package mb.stlcrec;

import mb.esv.common.ESVStylingRules;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class STLCRecStylingRules implements Serializable {
    final ESVStylingRules stylingRules;

    private STLCRecStylingRules(ESVStylingRules stylingRules) {
        this.stylingRules = stylingRules;
    }

    public static STLCRecStylingRules fromClassLoaderResources() throws IOException {
        final String resource = "mb/stlcrec/editor.esv.af";
        try(final @Nullable InputStream inputStream = STLCRecParseTable.class.getClassLoader().getResourceAsStream(
            resource)) {
            if(inputStream == null) {
                throw new RuntimeException(
                    "Cannot create styling rules; cannot find resource '" + resource + "' in classloader resources");
            }
            final ESVStylingRules stylingRules = ESVStylingRules.fromStream(inputStream);
            return new STLCRecStylingRules(stylingRules);
        }
    }
}
