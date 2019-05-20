package mb.jsglr.common;

import mb.common.region.Region;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.attachments.OriginAttachment;
import org.spoofax.terms.visitor.AStrategoTermVisitor;
import org.spoofax.terms.visitor.StrategoTermVisitee;

import java.util.ArrayList;

public class TermTracer {
    /**
     * Gets the originating term of given term, or null if it cannot be found.
     */
    public static @Nullable IStrategoTerm getOrigin(IStrategoTerm term) {
        return OriginAttachment.getOrigin(term);
    }

    /**
     * Gets the source code region given term originated from, or null if it cannot be found.
     */
    public static @Nullable Region getRegion(IStrategoTerm originatingTerm) {
        final IToken left = ImploderAttachment.getLeftToken(originatingTerm);
        final IToken right = ImploderAttachment.getRightToken(originatingTerm);
        if(left == null || right == null) {
            return null;
        }
        return RegionUtil.fromTokens(left, right);
    }

    /**
     * Gets the key of the resource given term originated from, or null if it cannot be found.
     */
    public static @Nullable ResourceKey getResourceKey(IStrategoTerm originatingTerm) {
        return ResourceKeyAttachment.getResourceKey(originatingTerm);
    }

    /**
     * Gets the term and its ancestors containing {@code region}, inside {@code term}. The returned list is ordered by
     * terms from the innermost (leaf) term to the outermost (root) term.
     */
    public static ArrayList<IStrategoTerm> getTermAndAncestorsContainingRegion(IStrategoTerm term, Region region) {
        final ArrayList<IStrategoTerm> found = new ArrayList<>();
        StrategoTermVisitee.bottomup(new AStrategoTermVisitor() {
            @Override public boolean visit(@NonNull IStrategoTerm term) {
                final @Nullable Region termRegion = getRegion(term);
                if(termRegion != null && termRegion.contains(region)) {
                    found.add(term);
                    return false; // TODO: bottom-up traversal ignores return value.
                }
                return true; // TODO: bottom-up traversal ignores return value.
            }
        }, term);
        return found;
    }

    /**
     * Gets the term and its descendants that are contained in {@code region}, inside {@code term}. The returned list is
     * ordered by terms from the outermost (root) to the innermost (leaf) term.
     */
    public static ArrayList<IStrategoTerm> termAndDescendantsContainedInRegion(IStrategoTerm term, Region region) {
        final ArrayList<IStrategoTerm> parsed = new ArrayList<>();
        StrategoTermVisitee.topdown(new AStrategoTermVisitor() {
            @Override public boolean visit(@NonNull IStrategoTerm term) {
                if(term.isList() && term.getSubtermCount() == 1) {
                    return true; // Do not return singleton lists, but continue topdown traversal into their singleton element.
                }
                final @Nullable Region termRegion = getRegion(term);
                if(termRegion != null && region.contains(termRegion)) {
                    parsed.add(term);
                    return false; // Do not traverse further down the tree to prevent children terms from being added.
                    // TODO: shouldn't they be added?
                }
                return true;
            }
        }, term);
        return parsed;
    }
}
