package mb.spoofax.runtime.term;

import mb.fs.java.JavaFSPath;
import mb.spoofax.api.region.Region;
import mb.spoofax.runtime.jsglr.RegionFactory;
import mb.spoofax.runtime.jsglr.SourcePathAttachment;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.attachments.OriginAttachment;

import javax.annotation.Nullable;

public class TermOrigin {
    public static @Nullable Region region(IStrategoTerm term) {
        final IStrategoTerm origin = origin(term);
        if(origin == null) {
            return tokenRegion(term);
        }
        return tokenRegion(origin);
    }

    public static @Nullable IStrategoTerm origin(IStrategoTerm term) {
        return OriginAttachment.getOrigin(term);
    }

    public static @Nullable JavaFSPath sourcePath(IStrategoTerm term) {
        return SourcePathAttachment.getPathForTerm(term);
    }


    private static @Nullable Region tokenRegion(IStrategoTerm term) {
        final IToken left = ImploderAttachment.getLeftToken(term);
        final IToken right = ImploderAttachment.getRightToken(term);
        if(left == null || right == null) {
            return null;
        }
        return RegionFactory.fromTokens(left, right);
    }
}
