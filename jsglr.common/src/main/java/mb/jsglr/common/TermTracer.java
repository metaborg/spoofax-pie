package mb.jsglr.common;

import mb.common.region.Region;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.attachments.OriginAttachment;

import java.util.Stack;

public class TermTracer {
    /**
     * Gets the originating term of given term, or null if it cannot be found.
     */
    public static @Nullable IStrategoTerm getOrigin(IStrategoTerm term) {
        return OriginAttachment.getOrigin(term);
    }

    /**
     * Gets the originating term of given term, or the term itself if it cannot be found.
     */
    public static IStrategoTerm tryGetOrigin(IStrategoTerm term) {
        return OriginAttachment.tryGetOrigin(term);
    }


    /**
     * Gets the source code region given term originated from, or null if it cannot be found.
     */
    public static @Nullable Region getRegion(IStrategoTerm term) {
        term = OriginAttachment.tryGetOrigin(term);
        final @Nullable IToken left = ImploderAttachment.getLeftToken(term);
        final @Nullable IToken right = ImploderAttachment.getRightToken(term);
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
     * Gets the smallest term from the {@code ast} that encompasses given {@code region}.
     *
     * @param ast    AST to select a term from.
     * @param region Selection region.
     * @return Smallest term that encompasses given region, or the entire AST if no terms have region information.
     */
    public static IStrategoTerm getSmallestTermEncompassingRegion(IStrategoTerm ast, Region region) {
        IStrategoTerm minimalTerm = ast;
        int minimalLength = Integer.MAX_VALUE;
        final Stack<IStrategoTerm> stack = new Stack<IStrategoTerm>();
        stack.push(ast);
        while(!stack.empty()) {
            final IStrategoTerm term = stack.pop();
            final @Nullable Region termRegion = getRegion(term);
            if(termRegion != null) {
                final int length = termRegion.length();
                if(termRegion.contains(region) && length < minimalLength) {
                    minimalTerm = term;
                    minimalLength = length;
                }
            }
            for(int i = term.getSubtermCount() - 1; i >= 0; --i) {
                stack.push(term.getSubterm(i));
            }
        }
        return minimalTerm;
    }
}
