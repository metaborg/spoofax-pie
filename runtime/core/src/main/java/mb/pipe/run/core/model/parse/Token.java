package mb.pipe.run.core.model.parse;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pipe.run.core.model.region.Region;

public interface Token extends Serializable {
    Region region();

    TokenType type();

    @Nullable IStrategoTerm associatedTerm();
    
    String textPart(String fullText);
}
