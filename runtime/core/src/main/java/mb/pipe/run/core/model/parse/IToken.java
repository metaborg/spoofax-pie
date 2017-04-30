package mb.pipe.run.core.model.parse;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pipe.run.core.model.region.IRegion;

public interface IToken extends Serializable {
    IRegion region();

    ITokenType type();

    @Nullable IStrategoTerm associatedTerm();
    
    String textPart(String fullText);
}
