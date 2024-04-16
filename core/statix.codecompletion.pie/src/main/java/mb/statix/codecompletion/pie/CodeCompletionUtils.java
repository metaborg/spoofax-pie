package mb.statix.codecompletion.pie;

import mb.common.region.Region;
import mb.common.util.ListView;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.ListTerms;
import mb.nabl2.terms.Terms;
import mb.nabl2.terms.stratego.TermOrigin;
import mb.nabl2.terms.stratego.TermPlaceholder;
import org.checkerframework.checker.nullness.qual.Nullable;
import mb.jsglr.shared.ImploderAttachment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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
        // If the token is empty or malformed, we skip it. (An empty token cannot contain a caret anyway.)
        if (endOffset <= startOffset) return null;

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
}
