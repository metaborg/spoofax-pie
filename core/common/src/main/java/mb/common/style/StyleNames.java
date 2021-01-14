package mb.common.style;

import mb.common.util.Experimental;
import mb.common.util.SetView;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * An ordered set of style names.
 *
 * A style name is interpreted by the environment, such as an IDE or the command line,
 * to give a specific style to a syntactic or semantic element, such as a source code token,
 * menu item, or code completion proposal.
 *
 * Often, multiple styles apply to the same element. For example, one style describes the syntactic kind
 * of element, and other styles modify the default style to indicate attributes such as
 * semantic attributes, the element being deprecated, the visibility of the element, and so on.
 * The style names of all these attributes are captured in this set.
 *
 * @see <a href="https://www.sublimetext.com/docs/3/scope_naming.html">Scope Naming</a>
 */
@Experimental
public final class StyleNames extends SetView<StyleName> implements Serializable {

    private static final StyleNames EMPTY = new StyleNames(new LinkedHashSet<>());

    /**
     * Gets the empty set of style names.
     *
     * @return the empty set of style names
     */
    public static StyleNames emptyStyleNames() { return EMPTY; }

    /**
     * Creates a new instance of the {@link StyleNames} class
     * with a single name.
     *
     * @param name the name
     * @return the created {@link StyleName} instance
     */
    public static StyleNames of(StyleName name) {
        final LinkedHashSet<StyleName> set = new LinkedHashSet<>();
        set.add(name);
        return new StyleNames(set);
    }

    /**
     * Creates a new instance of the {@link StyleNames} class.
     *
     * Note that duplicate names are removed.
     *
     * @param names the names, from most significant to least significant
     * @return the created {@link StyleName} instance
     */
    public static StyleNames of(StyleName... names) {
        final LinkedHashSet<StyleName> set = new LinkedHashSet<>();
        Collections.addAll(set, names);
        return new StyleNames(set);
    }

    /**
     * Initializes a new instance of the {@link StyleNames} class.
     *
     * @param set the set of style names, from most significant to least significant
     */
    private StyleNames(LinkedHashSet<StyleName> set) {
        // We assume the set does not contains null.
        // Also, we assume we are the only ones with control of the inner set.
        super(set);
    }

    /**
     * Gets whether this set of style names is empty.
     *
     * @return {@code true} when the set is empty;
     * otherwise, {@code false}.
     */
    public boolean isEmpty() {
        return this.collection.isEmpty();
    }

    /**
     * Determines whether this set contains any style name that starts with the specified style name.
     *
     * @param name the name to check
     * @return {@code true} when any style name in this set starts with the specified style name;
     * otherwise, {@code false}.
     */
    public boolean anyStartsWith(StyleName name) {
        return firstStartsWith(name) != null;
    }

    /**
     * Determines whether this set contains any style name that starts with the specified style name.
     *
     * @param name the name to check
     * @return {@code true} when any style name in this set starts with the specified style name;
     * otherwise, {@code false}.
     */
    public boolean anyStartsWith(String name) {
        return firstStartsWith(name) != null;
    }

    /**
     * Finds the first style name for which the specified name is a prefix.
     *
     * @param name the name to check
     * @return the first style name for which the specified name is a prefix;
     * otherwise, {@code null}
     */
    public @Nullable StyleName firstStartsWith(StyleName name) {
        if (this.collection.isEmpty()) return null;
        return this.collection.stream().filter(n -> n.startsWith(name)).findFirst().orElse(null);
    }

    /**
     * Finds the first style name for which the specified name is a prefix.
     *
     * @param name the name to check
     * @return the first style name for which the specified name is a prefix;
     * otherwise, {@code null}
     */
    public @Nullable StyleName firstStartsWith(String name) {
        @Nullable StyleName styleName = StyleName.fromString(name);
        if (styleName == null) throw new IllegalArgumentException("The name is not a valid style name.");
        return firstStartsWith(styleName);
    }

}
