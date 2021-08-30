package mb.statix.utils;

import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Functions for working with different word cases (e.g., title case, snake case, camel case, kebab case).
 */
public final class CaseFormat {
    private CaseFormat() { /* Cannot be instantiated. */ }

    /**
     * Splits a CamelCase sentence into its words.
     *
     * @param s the sentence to split
     * @return the words in the sentence
     */
    public static String[] splitCamelCase(String s) {
        return Pattern.compile(String.format("%s|%s|%s",
            "(?<=[A-Z])(?=[A-Z][a-z])",
            "(?<=[^A-Z])(?=[A-Z])",
            "(?<=[A-Za-z])(?=[^A-Za-z])"
        )).split(s);
    }

    /**
     * Combines a number of words into a kebab-case sentence.
     *
     * @param words the words in the sentence
     * @return the resulting sentence
     */
    public static String combineKebabCase(String[] words) {
        return combine(
            words,
            word -> word.toLowerCase(Locale.ROOT),
            word -> word.toLowerCase(Locale.ROOT),
            (sb, word) -> sb.append('-').append(word)
        );
    }

    /**
     * Combines a number of words into a snake_case sentence.
     *
     * @param words the words in the sentence
     * @return the resulting sentence
     */
    public static String combineSnakeCase(String[] words) {
        return combine(
            words,
            word -> word.toLowerCase(Locale.ROOT),
            word -> word.toLowerCase(Locale.ROOT),
            (sb, word) -> sb.append('_').append(word)
        );
    }

    /**
     * Combines a number of words into a sentence.
     *
     * @param words the words in the sentence
     * @param firstTransformation the first word transformation
     * @param transformation the subsequent word transformation
     * @param combiner the combiner
     * @return the resulting sentence
     */
    private static String combine(
        String[] words,
        Function<String, String> firstTransformation,
        Function<String, String> transformation,
        BiConsumer<StringBuilder, String> combiner
    ) {
        if (words.length == 0) return "";
        final StringBuilder sb = new StringBuilder();
        sb.append(firstTransformation.apply(words[0]));
        for (int i = 1; i < words.length; i++) {
            final String word = words[i];
            combiner.accept(sb, transformation.apply(word));
        }
        return sb.toString();
    }

}
