package mb.spoofax.runtime.model.parse;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.spoofax.runtime.model.region.Region;

public interface Token extends Serializable {
    Region region();

    TokenType type();

    @Nullable IStrategoTerm associatedTerm();


    default String textPart(String fullText) {
        final Region region = region();
        return fullText.substring(region.startOffset(), region.endOffset() + 1);
    }
}
