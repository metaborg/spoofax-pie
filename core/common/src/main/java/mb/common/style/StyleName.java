package mb.common.style;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A style name.
 *
 * The style of an element determines how a syntactic or semantic element is rendered by the
 * environment (an IDE, or on the command line). Elements include, but are not limited to,
 * tokens in source code, completion suggestions in code completion, and menu items.
 * The style of an element is usually determined by the environment (IDE), which renders such
 * elements in a IDE-specific way. However, different kinds of elements used in different languages
 * might require their own distinct IDE-specific style. Therefore a style name is used such
 * that the combination of style name and IDE is used to select the actual style to be used.
 *
 * A style name is a qualified hierarchical name, separated by dots, that goes from most general to most
 * specific. For example, "comment.block.documentation.java" is the style name for Java Doc comment
 * blocks. Similar to CSS classes and class selectors, an IDE might have a style for comments,
 * of the "comment" style name, but a slightly different style for "comment.block.documentation" code.
 * Perhaps the IDE even supports styling JavaDoc comments differently from other language's block comments.
 *
 * Style names are called "scope names" in Textmate and Sublime, but we'd like to avoid this confusing name.
 *
 * @see <a href="https://www.sublimetext.com/docs/3/scope_naming.html">Scope Naming</a>
 */
public final class StyleName implements Serializable {

    private static final String DEFAULT_STRING = "<default>";
    private static final StyleName DEFAULT = new StyleName(new String[0]);

    /**
     * Gets the default style name.
     *
     * @return the default style name, which has no parts
     */
    public static StyleName defaultStyleName() {
        return DEFAULT;
    }

    /**
     * Determines whether the given part name is valid.
     *
     * Part names must start with a letter or underscore,
     * followed by zero or more letters, digits, dashes, or underscores.
     *
     * @param partName the name to check
     * @return {@code true} when the name is valid; otherwise, {@code false}
     */
    public static boolean isValidPartName(String partName) {
        return partName.matches("[a-zA-Z_][a-zA-Z0-9\\-_]*");
    }

    /**
     * Creates a new instance of the {@link StyleName} class.
     *
     * @param parts the individual parts, in order from most to least specific (usually ending with the language name)
     * @return the created {@link StyleName} instance
     */
    public static StyleName of(String... parts) {
        if (parts.length == 0) return defaultStyleName();
        return new StyleName(parts);
    }

    /**
     * Creates a new instance of the {@link StyleName} class.
     *
     * @param parts the individual parts, in order from most to least specific (usually ending with the language name)
     * @return the created {@link StyleName} instance
     */
    public static StyleName of(Iterable<String> parts) {
        return of(StreamSupport.stream(parts.spliterator(), false).toArray(String[]::new));
    }

    /**
     * Creates a new instance of the {@link StyleName} class
     * by parsing the specified string.
     *
     * @param str the string to parse, which is a sequence of part names each separated by a dot (".")
     * @return the created {@link StyleName} instance; or {@code null} when the input could not be parsed
     */
    public static @Nullable StyleName fromString(String str) {
        if (str.isEmpty() || DEFAULT_STRING.equals(str)) return defaultStyleName();

        String[] parts = str.split("\\.");
        try {
            return new StyleName(parts);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** The list of parts, which may be empty. */
    private final String[] parts;

    /**
     * Initializes a new instance of the {@link StyleName} class.
     *
     * @param parts an array of part names
     */
    private StyleName(String[] parts) {
        if (!Arrays.stream(parts).allMatch(StyleName::isValidPartName)) {
            throw new IllegalArgumentException("The following part names are invalid: "
                + Arrays.stream(parts).filter(p -> !isValidPartName(p)).collect(Collectors.joining(", ")));
        }
        this.parts = parts.clone();
    }

    /**
     * Gets whether this style name denotes the default.
     *
     * @return {@code true} when this style name has no parts;
     * otherwise, {@code false}.
     */
    public boolean isDefault() {
        return this.parts.length == 0;
    }

    /**
     * Determines whether this style name starts with the specified style name.
     *
     * @param name the name to check
     * @return {@code true} when this style name starts with the specified style name;
     * otherwise, {@code false}.
     */
    public boolean startsWith(StyleName name) {
        if (name.parts.length > this.parts.length) return false;
        for (int i = 0; i < name.parts.length; i++) {
            if (!this.parts[i].equals(name.parts[i]))
                return false;
        }
        return true;
    }

    /**
     * Determines whether this style name starts with the specified style name.
     *
     * @param name the name to check
     * @return {@code true} when this style name starts with the specified style name;
     * otherwise, {@code false}.
     */
    public boolean startsWith(String name) {
        @Nullable StyleName styleName = StyleName.fromString(name);
        if (styleName == null)
            throw new IllegalArgumentException("The specified name is not a valid style name.");
        return startsWith(styleName);
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        StyleName styleName = (StyleName)o;
        return Arrays.equals(parts, styleName.parts);
    }

    @Override public int hashCode() {
        return Arrays.hashCode(parts);
    }

    @Override public String toString() {
        return !isDefault() ? String.join(".", this.parts) : DEFAULT_STRING;
    }
}
