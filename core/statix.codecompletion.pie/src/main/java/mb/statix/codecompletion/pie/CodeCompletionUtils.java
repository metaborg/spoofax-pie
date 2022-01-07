package mb.statix.codecompletion.pie;

import mb.common.region.Region;
import mb.common.util.ListView;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.ListTerms;
import mb.nabl2.terms.Terms;
import mb.nabl2.terms.stratego.TermOrigin;
import mb.nabl2.terms.stratego.TermPlaceholder;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.util.TermUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility methods used by the code completion algorithm.
 */
final class CodeCompletionUtils {
    private CodeCompletionUtils() { /* Cannot be instantiated. */ }

    /**
     * Replaces all strings of layout (newlines, spaces) with a single space.
     *
     * @param text the text to normalize
     * @return the normalized text
     */
    public static String normalizeText(String text) {
        // TODO: We should probably support using the template's whitespace
        return text.replaceAll("\\s+", " ");
    }

    /**
     * Returns the qualified name of the rule.
     *
     * @param specName the name of the specification
     * @param ruleName the name of the rule
     * @return the qualified name of the rule, in the form of {@code &lt;specName&gt;!&lt;ruleName&gt;}.
     */
    public static String makeQualifiedName(String specName, String ruleName) {
        if (specName.equals("") || ruleName.contains("!")) return ruleName;
        return specName + "!" + ruleName;
    }

    /**
     * Finds the placeholder near the caret location in the specified term.
     *
     * This method assumes all terms in the term are uniquely identifiable,
     * for example through a term index or unique tree path.
     *
     * @param term the term (an AST with placeholders)
     * @param caretOffset the caret location
     * @return the placeholder; or {@code null} if not found
     */
    public static @Nullable ITermVar findPlaceholderAt(ITerm term, int caretOffset) {
        if (!termContainsCaret(term, caretOffset)) return null;
        // Recurse into the term
        return term.match(Terms.cases(
            (appl) -> appl.getArgs().stream().map(a -> findPlaceholderAt(a, caretOffset)).filter(Objects::nonNull).findFirst().orElse(null),
            (list) -> list.match(ListTerms.cases(
                (cons) -> {
                    @Nullable final ITermVar headMatch = findPlaceholderAt(cons.getHead(), caretOffset);
                    if (headMatch != null) return headMatch;
                    return findPlaceholderAt(cons.getTail(), caretOffset);
                },
                (nil) -> null,
                (var) -> null
            )),
            (string) -> null,
            (integer) -> null,
            (blob) -> null,
            (var) -> isPlaceholder(var) ? var : null
        ));
    }

    /**
     * Finds all placeholders near the selection in the specified term.
     *
     * This method assumes all terms in the term are uniquely identifiable,
     * for example through a term index or unique tree path.
     *
     * @param term the term (an AST with placeholders)
     * @param selection the selection
     * @return the found placeholders; or an empty list if none where found
     */
    public static List<? extends ITermVar> findAllPlaceholdersIn(ITerm term, Region selection) {
        if (!termContainsSelection(term, selection)) return Collections.emptyList();

        // Recurse into the term
        return term.match(Terms.cases(
            (appl) -> appl.getArgs().stream().flatMap(a -> findAllPlaceholdersIn(a, selection).stream())
                .collect(Collectors.toList()),
            (list) -> list.match(ListTerms.cases(
                (cons) -> {
                    final ArrayList<ITermVar> foundPlaceholders = new ArrayList<>();
                    foundPlaceholders.addAll(findAllPlaceholdersIn(cons.getHead(), selection));
                    foundPlaceholders.addAll(findAllPlaceholdersIn(cons.getTail(), selection));
                    return foundPlaceholders;
                },
                (nil) -> null,
                (var) -> null
            )),
            (string) -> null,
            (integer) -> null,
            (blob) -> null,
            (var) -> isPlaceholder(var) ? Collections.singletonList(var) : null
        ));
    }

    /**
     * Determines whether the specified term contains the specified caret offset.
     *
     * @param term the term
     * @param caretOffset the caret offset to find
     * @return {@code true} when the term contains the caret offset;
     * otherwise, {@code false}.
     */
    public static boolean termContainsCaret(ITerm term, int caretOffset) {
        @Nullable Region region = tryGetRegion(term);
        if (region == null) {
            // One of the children must contain the caret
            return term.match(Terms.cases(
                (appl) -> appl.getArgs().stream().anyMatch(a -> termContainsCaret(a, caretOffset)),
                (list) -> list.match(ListTerms.cases(
                    (cons) -> {
                        final boolean headContains = termContainsCaret(cons.getHead(), caretOffset);
                        if (headContains) return true;
                        return termContainsCaret(cons.getTail(), caretOffset);
                    },
                    (nil) -> false,
                    (var) -> false
                )),
                (string) -> false,
                (integer) -> false,
                (blob) -> false,
                (var) -> false
            ));
        }
        return region.contains(caretOffset);
    }

    /**
     * Determines whether the specified term intersects with the specified selection.
     *
     * @param term the term
     * @param selection the selection to find
     * @return {@code true} when the term intersects with the selection;
     * otherwise, {@code false}.
     */
    public static boolean termContainsSelection(ITerm term, Region selection) {
        @Nullable Region region = getAdjacentRegionOf(term);
        if (region == null) {
            // One of the children must contain the caret
            return term.match(Terms.cases(
                (appl) -> appl.getArgs().stream().anyMatch(a -> termContainsSelection(a, selection)),
                (list) -> list.match(ListTerms.cases(
                    (cons) -> {
                        final boolean headContains = termContainsSelection(cons.getHead(), selection);
                        if (headContains) return true;
                        return termContainsSelection(cons.getTail(), selection);
                    },
                    (nil) -> false,
                    (var) -> false
                )),
                (string) -> false,
                (integer) -> false,
                (blob) -> false,
                (var) -> false
            ));
        }
        return region.intersectsWith(selection);
    }

    /**
     * Attempts to get the region occupied by the specified term.
     *
     * @param term the term
     * @return the term's region; or {@code null} when it could not be determined
     */
    public static @Nullable Region tryGetRegion(ITerm term) {
        @Nullable final TermOrigin origin = TermOrigin.get(term).orElse(null);
        if (origin == null) return null;
        final ImploderAttachment imploderAttachment = origin.getImploderAttachment();
        // We get the zero-based offset of the first character in the token
        int startOffset = imploderAttachment.getLeftToken().getStartOffset();
        // We get the zero-based offset of the character following the token, which is why we have to add 1
        int endOffset = imploderAttachment.getRightToken().getEndOffset() + 1;
        // If the token is malformed, we skip it. Empty regions are allowed, as they are used for the inserted placeholders.
        if (endOffset < startOffset) return null;

        return Region.fromOffsets(
            startOffset,
            endOffset
        );
    }

    /**
     * Attempts to get the region occupied by the specified term.
     *
     * @param term the term
     * @param defaultRegion the region to use if it could not be determined from the term
     * @return the term's region; or the specified default region when it could not be determined
     */
    public static Region getRegion(ITerm term, Region defaultRegion) {
        @Nullable final Region region = tryGetRegion(term);
        if (region != null) return region;
        return defaultRegion;
    }

    /**
     * Determines whether the given term variable is a placeholder term variable.
     *
     * @param var the term variable
     * @return {@code true} when the term variabnle is a placeholder term variable;
     * otherwise, {@code false}
     */
    public static boolean isPlaceholder(ITermVar var) {
        return TermPlaceholder.has(var);
    }

    /**
     * Converts an iterable to a list.
     *
     * @param iterable the iterable to convert
     * @param <T>      the type of elements in the iterable
     * @return a list with the elements of the iterable
     */
    public static <T> List<T> iterableToList(Iterable<T> iterable) {
        if(iterable instanceof List) {
            // It's already a List<T>.
            return (List<T>)iterable;
        } else if(iterable instanceof Collection) {
            // It's a Collection<T>.
            return new ArrayList<>((Collection<T>)iterable);
        } else {
            final Iterator<T> iterator = iterable.iterator();
            if(!iterator.hasNext()) {
                // It's an empty Iterable<T>.
                return Collections.emptyList();
            } else {
                // It's a non-empty Iterable<T>.
                final ArrayList<T> list = new ArrayList<>();
                while(iterator.hasNext()) {
                    list.add(iterator.next());
                }
                return list;
            }
        }
    }

    /**
     * Converts an iterable to a {@link ListView}.
     *
     * @param iterable the iterable to convert
     * @param <T>      the type of elements in the iterable
     * @return a list with the elements of the iterable
     */
    public static <T> ListView<T> iterableToListView(Iterable<T> iterable) {
        if(iterable instanceof ListView) {
            // It's already a ListView<T>.
            return (ListView<T>)iterable;
        } else {
            // It's something else.
            return new ListView<T>(iterableToList(iterable));
        }
    }


    /**
     * Determines the adjacent region of the specified term fragment, including surrounding layout if necessary.
     *
     * The adjacent region of a term is the region of a term optionally expanded to include
     * layout/error tokens to its left and/or right.
     *
     * @param fragmentTerm the fragment term
     * @return the region of the term; or {@code null} when the region could not be determined
     */
    public static @Nullable Region getAdjacentRegionOf(IStrategoTerm fragmentTerm) {
        final @Nullable Fragment fragment = StrategoTermFragment.fromTerm(fragmentTerm);
        if(fragment == null) return null;
        return getAdjacentRegionOf(fragment);
    }

    /**
     * Determines the adjacent region of the specified term fragment, including surrounding layout if necessary.
     *
     * The adjacent region of a term is the region of a term optionally expanded to include
     * layout/error tokens to its left and/or right.
     *
     * @param fragmentTerm the fragment term
     * @return the region of the term; or {@code null} when the region could not be determined
     */
    public static @Nullable Region getAdjacentRegionOf(ITerm fragmentTerm) {
        final @Nullable Fragment fragment = NablTermFragment.fromTerm(fragmentTerm);
        if(fragment == null) return null;
        return getAdjacentRegionOf(fragment);
    }

    /**
     * Determines the adjacent region of the specified term fragment, including surrounding layout if necessary.
     *
     * The adjacent region of a term is the region of a term optionally expanded to include
     * layout/error tokens to its left and/or right.
     *
     * @param fragment the fragment
     * @return the region of the term; or {@code null} when the region could not be determined
     * @see org.metaborg.spoofax.core.completion.JSGLRCompletionService#fromTokens
     */
    public static @Nullable Region getAdjacentRegionOf(Fragment fragment) {
        // FIXME: Not sure why the JSGLRCompletionService checks whether the term's sort
        //  is left- or right-recursive. Especially since this causes the layout on the other side to be included?
        boolean isLeftRecursive = false;//fragment.isLeftRecursive();
        boolean isRightRecursive = false;//fragment.isRightRecursive();

        // We will change these tokens to expand or contract the selection
        IToken leftToken = fragment.getLeftToken();
        IToken rightToken = fragment.getRightToken();

        final boolean includeLeftLayout = isRightRecursive || fragment.isList() || fragment.isOptional();
        if (includeLeftLayout) {
            leftToken = includeLeftLayout(leftToken);
        } else {
            leftToken = excludeLeftLayout(leftToken, rightToken);
        }

        final boolean includeRightLayout = isLeftRecursive || fragment.isList() || fragment.isOptional();
        if (includeRightLayout) {
            rightToken = includeRightLayout(rightToken);
        } else {
            rightToken = excludeRightLayout(leftToken, rightToken);
        }

        // FIXME: Do we need to fix the difference between the offset and the cursor position?
        // Taken from JSGLRSourceRegionFactory.fromTokensLayout()
        final int leftOffset = leftToken.getStartOffset(); // FIXME: + (leftToken.getKind() != IToken.Kind.TK_LAYOUT && !fragment.isNullable() ? 1 : 0);
        final int rightOffset = rightToken.getEndOffset(); // FIXME: + (fragment.isNullable() ? 1 : 0);

        return Region.fromOffsets(
            leftOffset, rightOffset + 1,
            leftToken.getLine(), rightToken.getLine()
        );
    }

    /**
     * Includes the layout/error tokens to the left of the selected tokens.
     *
     * @param leftToken the left-most selected token
     * @return the new left-most selected token, that includes layout (or errors)
     */
    private static IToken includeLeftLayout(IToken leftToken) {
        @Nullable IToken currentToken = leftToken.getTokenBefore();
        IToken newLeftToken = leftToken;
        while (currentToken != null) {
            if (!isLayout(currentToken)) break;
            newLeftToken = currentToken;
            currentToken = currentToken.getTokenBefore();
        }
        return newLeftToken;
    }

    /**
     * Includes the layout/error tokens to the right of the selected tokens.
     *
     * @param rightToken the right-most selected token
     * @return the new right-most selected token, that includes layout (or errors or EOF)
     */
    private static IToken includeRightLayout(IToken rightToken) {
        @Nullable IToken currentToken = rightToken.getTokenAfter();
        IToken newRightToken = rightToken;
        while (currentToken != null) {
            if (!isLayout(currentToken)) break;
            newRightToken = currentToken;
            currentToken = currentToken.getTokenAfter();
        }
        return newRightToken;
    }

    /**
     * Excludes the layout/error tokens to the left of the selected tokens.
     *
     * @param leftToken  the left-most selected token
     * @param rightToken the right-most selected token
     * @return the new left-most selected token, that does not include layout (or errors or EOF)
     */
    private static IToken excludeLeftLayout(IToken leftToken, IToken rightToken) {
        @Nullable IToken currentToken = leftToken;
        IToken newLeftToken = leftToken;
        while (currentToken != null && currentToken != rightToken) {
            if (!isLayout(currentToken)) break;
            newLeftToken = currentToken;
            currentToken = currentToken.getTokenAfter();
        }
        return newLeftToken;
    }

    /**
     * Excludes the layout/error tokens to the right of the selected tokens.
     *
     * @param leftToken  the left-most selected token
     * @param rightToken the right-most selected token
     * @return the new right-most selected token, that does not include layout (or errors)
     */
    private static IToken excludeRightLayout(IToken leftToken, IToken rightToken) {
        @Nullable IToken currentToken = rightToken;
        IToken newRightToken = rightToken;
        while (currentToken != null && currentToken != leftToken) {
            if (!isLayout(currentToken)) break;
            newRightToken = currentToken;
            currentToken = currentToken.getTokenBefore();
        }
        return newRightToken;
    }

    /**
     * Gets whether the token is a layout/error token.
     *
     * @return {@code true} when the token denotes layout or an error;
     * otherwise, {@code false}
     */
    private static boolean isLayout(IToken token) {
        switch (token.getKind()) {
            case TK_LAYOUT:
            case TK_EOF:
            case TK_ERROR:
            case TK_ERROR_LAYOUT:
                return true;
            default:
                return false;
        }
    }

    /**
     * Determines if the selection is wholly contained within the fragment,
     * and whether the sort of the fragment is left-recursive.
     *
     * @param fragment  the fragment whose sort to check
     * @param selection the selection
     * @return {@code true} when the fragment's sort is left-recursive;
     * otherwise, {@code false}; or {@code false} when it could not be determined
     */
    public static boolean isLeftRecursive(StrategoTermFragment fragment, Region selection, StrategoRuntime strategoRuntime) {
        if(TermUtils.isAppl(fragment.getTerm()) && selection.getEndOffset() >= fragment.getRightOffset()) {
            try {
                @Nullable final IStrategoTerm output = strategoRuntime.invokeOrNull("is-left-recursive", strategoRuntime.getTermFactory().makeString(fragment.getSort()));
                return output != null;
            } catch(StrategoException ex) {
                // Failed to check whether term is left-recursive.
                return false;
            }
        }
        return false;
    }

    /**
     * Determines if the selection is wholly contained within the fragment,
     * and whether the sort of the fragment is right-recursive.
     *
     * @param fragment  the fragment whose sort to check
     * @param selection the selection
     * @return {@code true} when the fragment's sort is right-recursive;
     * otherwise, {@code false}; or {@code false} when it could not be determined
     */
    public static boolean isRightRecursive(StrategoTermFragment fragment, Region selection, StrategoRuntime strategoRuntime) {
        // FIXME: Not sure why the JSGLRCompletionService checks whether the term
        //  is near the selection and right-recursive.
        //  Especially since this causes the layout on the _left_ side to be included.
        if(TermUtils.isAppl(fragment.getTerm()) && selection.getStartOffset() <= fragment.getLeftOffset()) {
            try {
                @Nullable final IStrategoTerm output = strategoRuntime.invokeOrNull("is-right-recursive", strategoRuntime.getTermFactory().makeString(fragment.getSort()));
                return output != null;
            } catch(StrategoException ex) {
                // Failed to check whether term is right-recursive.
                return false;
            }
        }
        return false;
    }
}
