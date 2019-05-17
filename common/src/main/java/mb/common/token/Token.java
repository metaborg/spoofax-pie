package mb.common.token;

import mb.common.region.Region;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;

public interface Token extends Serializable {
    // DP: getRegion() because of JavaBeans and Kotlin
    Region region();

    // DP: getType() idem.
    TokenType type();

    // DP: getAssociatedTerm()
    @Nullable IStrategoTerm associatedTerm();

    default String textPart(String fullText) {
        final Region region = region();
        return fullText.substring(region.startOffset, region.endOffset + 1);
    }
}
