package mb.jsglr.common;

import mb.common.region.Region;
import mb.common.util.ListView;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.attachments.OriginAttachment;
import org.spoofax.terms.visitor.AStrategoTermVisitor;
import org.spoofax.terms.visitor.IStrategoTermVisitor;
import org.spoofax.terms.visitor.StrategoTermVisitee;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
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
     * Gets the source code region given term originated from, automatically getting the originating term if needed.
     * If the term is inside a fragment, the region will be relative to the start of the fragment,
     * instead of relative to the start of the SPT file.
     *
     * @return Region, or {@code null} if no region could be found.
     */
    public static @Nullable Region getInFragmentRegion(IStrategoTerm term) {
        term = ImploderAttachment.getImploderOrigin(term);
        if(term == null) return null;
        final @Nullable IToken left = ImploderAttachment.getLeftToken(term);
        final @Nullable IToken right = ImploderAttachment.getRightToken(term);
        if(left == null || right == null) return null;
        return RegionUtil.fromTokens(
            FragmentedOriginLocationFixer.getOriginalToken(left),
            FragmentedOriginLocationFixer.getOriginalToken(right)
        );
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
     * If the AST is part of a fragment, the region should be relative to the start of the fragment,
     * not relative to the SPT file.
     *
     * @param ast    AST to select a term from.
     * @param region Selection region. If in a fragment, relative to the fragment start.
     * @return Smallest term that encompasses given region, or the entire AST if no terms have region information.
     */
    public static IStrategoTerm getSmallestTermEncompassingRegion(IStrategoTerm ast, Region region) {
        IStrategoTerm minimalTerm = ast;
        int minimalLength = Integer.MAX_VALUE;
        final Stack<IStrategoTerm> stack = new Stack<IStrategoTerm>();
        stack.push(ast);
        while(!stack.empty()) {
            final IStrategoTerm term = stack.pop();
            final @Nullable Region termRegion = getInFragmentRegion(term);
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
     * Gets the term that spans the largest region from the {@code ast} that resides inside given {@code region}.
     * If the AST is part of a fragment, the region should be relative to the start of the fragment,
     * not relative to the SPT file.
     *
     * @param ast    AST to select a term from.
     * @param region Selection region. If in a fragment, relative to the fragment start.
     * @return Biggest term that resides inside given region, or the entire AST if no terms have region information.
     */
    public static IStrategoTerm getBiggestTermInsideRegion(IStrategoTerm ast, Region region) {
        IStrategoTerm maxTerm = ast;
        int maxLength= -1;
        final Stack<IStrategoTerm> stack = new Stack<>();
        stack.push(ast);
        while(!stack.empty()) {
            final IStrategoTerm term = stack.pop();
            final @Nullable Region termRegion = getInFragmentRegion(term);
            if(termRegion != null && region.contains(termRegion)) {
                final int length = termRegion.getLength();
                if (length > maxLength) {
                    maxTerm = term;
                    maxLength = length;
                }
                // If a term fits inside the region, do not evaluate it's subterms, since these will always be smaller
            } else {
                for(int i = term.getSubtermCount() - 1; i >= 0; --i) {
                    stack.push(term.getSubterm(i));
                }
            }
        }
        return maxTerm;
    }

    /**
     * Gets the terms from the {@code ast} that resides inside given {@code region} in order of occurrence.
     * If a term occurs inside the region, it's subterms won't be included in the result.
     * If the AST is part of a fragment, the region should be relative to the start of the fragment,
     * not relative to the SPT file.
     *
     * @param ast    AST to select a term from.
     * @param region Selection region. If in a fragment, relative to the fragment start.
     * @return Terms that reside inside given region.
     */
    public static ListView<IStrategoTerm> getTermsInsideRegion(IStrategoTerm ast, Region region) {
        final Stack<IStrategoTerm> stack = new Stack<>();
        final List<IStrategoTerm> terms = new ArrayList<>();
        stack.push(ast);
        while(!stack.empty()) {
            final IStrategoTerm term = stack.pop();
            final @Nullable Region termRegion = getInFragmentRegion(term);
            if(termRegion != null && region.contains(termRegion)) {
                terms.add(term);
            } else {
                for(int i = term.getSubtermCount() - 1; i >= 0; --i) {
                    stack.push(term.getSubterm(i));
                }
            }
        }
        return ListView.of(terms);
    }

    /**
     * Gets all terms that contain the specified region. These terms are ordered
     * in a bottom-up fashion, such that the first element is the deepest nested
     * (and therefore smallest) element that contains the given region. Will always
     * return at least one element as long as the specified region is contained
     * within the AST.
     *
     * @param ast AST to find terms in
     * @param region the region that terms should be in. The entire region must
     *               fit within the term in order for it to be returned
     * @return all terms that contain the given region, ordered in bottom-up fashion
     */
    public static Collection<IStrategoTerm> getTermsEncompassingRegion(IStrategoTerm ast, Region region) {
        final Collection<IStrategoTerm> parsed = new LinkedList<>();
        final IStrategoTermVisitor visitor = new AStrategoTermVisitor() {
            @Override public boolean visit(IStrategoTerm term) {
                final @Nullable Region location = TermTracer.getRegion(term);
                if(location != null && location.contains(region)) {
                    parsed.add(term);
                    return false;
                }
                return true;
            }
        };
        StrategoTermVisitee.bottomup(visitor, ast);
        return parsed;
    }
}
