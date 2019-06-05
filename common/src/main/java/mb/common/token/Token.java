package mb.common.token;

import mb.common.region.Region;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;

public interface Token extends Serializable {
    Region getRegion();

    TokenType getType();

    @Nullable IStrategoTerm getAssociatedTerm();

    default String textPart(String fullText) {
        final Region region = getRegion();
        return fullText.substring(region.startOffset, region.endOffset + 1);
    }
}
