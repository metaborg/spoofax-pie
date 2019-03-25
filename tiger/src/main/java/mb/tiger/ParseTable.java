package mb.tiger;

import mb.jsglr1.common.JSGLR1ParseTable;
import mb.jsglr1.common.JSGLR1ParseTableException;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.InputStream;

public class ParseTable {
    final JSGLR1ParseTable internalParseTable;

    public ParseTable() throws JSGLR1ParseTableException, IOException {
        final String resource = "target/metaborg/sdf.tbl";
        try(final @Nullable InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resource)) {
            if(inputStream == null) {
                throw new RuntimeException(
                    "Cannot create parse table; cannot find resource '" + resource + "' in classloader resources");
            }
            this.internalParseTable = new JSGLR1ParseTable(inputStream);
        }
    }
}
