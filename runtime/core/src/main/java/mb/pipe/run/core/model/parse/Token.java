package mb.pipe.run.core.model.parse;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pipe.run.core.model.region.IRegion;

public class Token implements IToken {
    private static final long serialVersionUID = 1L;

    private final IRegion region;
    private final ITokenType type;
    private final @Nullable IStrategoTerm associatedTerm;


    public Token(IRegion region, ITokenType type, @Nullable IStrategoTerm associatedTerm) {
        this.region = region;
        this.type = type;
        this.associatedTerm = associatedTerm;
    }


    @Override public IRegion region() {
        return region;
    }

    @Override public ITokenType type() {
        return type;
    }

    @Override public @Nullable IStrategoTerm associatedTerm() {
        return associatedTerm;
    }

    @Override public String textPart(String fullText) {
        return fullText.substring(region.startOffset(), region.endOffset() + 1);
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + region.hashCode();
        result = prime * result + type.hashCode();
        result = prime * result + ((associatedTerm == null) ? 0 : associatedTerm.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final Token other = (Token) obj;
        if(!region.equals(other.region))
            return false;
        if(!type.equals(other.type))
            return false;
        if(associatedTerm == null) {
            if(other.associatedTerm != null)
                return false;
        } else if(!associatedTerm.equals(other.associatedTerm))
            return false;
        return true;
    }

    @Override public String toString() {
        return region.toString();
    }
}
