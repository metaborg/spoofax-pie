package mb.spoofax.intellij;

import com.intellij.openapi.util.TextRange;


/**
 * Utility functions for working with {@link Span} in IntelliJ.
 */
public final class SpanUtils {

    public TextRange spanToTextRange(Span span) {
        return new TextRange(span.getStartOffset(), span.getEndOffset());
    }

}
