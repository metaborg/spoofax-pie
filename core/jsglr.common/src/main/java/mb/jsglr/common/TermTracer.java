package mb.jsglr.common;

import mb.common.region.Region;
import mb.common.util.ListView;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.attachments.OriginAttachment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
     * Gets the source code region given term originated from, automatically getting the originating term if needed.
     *
     * @return Region, or {@code null} if no region could be found.
     */
    public static @Nullable Region getRegion(IStrategoTerm term) {
        term = ImploderAttachment.getImploderOrigin(term);
        if(term == null) return null;
        final @Nullable IToken left = ImploderAttachment.getLeftToken(term);
        final @Nullable IToken right = ImploderAttachment.getRightToken(term);
        if(left == null || right == null) return null;
        return RegionUtil.fromTokens(left, right);
    }

    public static Optional<Region> getRegionOptional(IStrategoTerm term) {
        return Optional.ofNullable(getRegion(term));
    }

    /**
     * Gets the key of the resource given term originated from, automatically getting the originating term if needed.
     *
     * @return Resource key, or {@code null} if no resource key could be found.
     */
    public static @Nullable ResourceKey getResourceKey(IStrategoTerm term) {
        term = ImploderAttachment.getImploderOrigin(term);
        if(term == null) return null;
        return ResourceKeyAttachment.getResourceKey(term);
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
                final int length = termRegion.getLength();
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

    /**
     * Gets the biggest term from the {@code ast} that resides inside given {@code region}.
     *
     * @param ast    AST to select a term from.
     * @param region Selection region.
     * @return Biggest term that resides inside given region, or the entire AST if no terms have region information.
     */
    public static IStrategoTerm getBiggestTermInsideRegion(IStrategoTerm ast, Region region) {
        final Stack<IStrategoTerm> stack = new Stack<>();
        stack.push(ast);
        while(!stack.empty()) {
            final IStrategoTerm term = stack.pop();
            final @Nullable Region termRegion = getRegion(term);
            if(termRegion != null && region.contains(termRegion)) {
                return term;
            }
            for(int i = term.getSubtermCount() - 1; i >= 0; --i) {
                stack.push(term.getSubterm(i));
            }
        }
        return ast;
    }

    /**
     * Gets the outermost terms from the {@code ast} that resides inside given {@code region}.
     *
     * @param ast    AST to select a term from.
     * @param region Selection region.
     * @return Outermost terms that reside inside given region.
     */
    public static ListView<IStrategoTerm> getTermsInsideRegion(IStrategoTerm ast, Region region) {
        final Stack<IStrategoTerm> stack = new Stack<>();
        final List<IStrategoTerm> terms = new ArrayList<>();
        stack.push(ast);
        while(!stack.empty()) {
            final IStrategoTerm term = stack.pop();
            final @Nullable Region termRegion = getRegion(term);
            if(termRegion != null && region.contains(termRegion)) {
                terms.add(term);
                continue;
            }
            for(int i = term.getSubtermCount() - 1; i >= 0; --i) {
                stack.push(term.getSubterm(i));
            }
        }
        return ListView.of(terms);
    }
}
