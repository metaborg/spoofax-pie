package mb.common.style;

import mb.common.util.Experimental;
import mb.common.util.SetView;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A set of style names.
 *
 * A style name is interpreted by the environment, such as an IDE or the command line,
 * to give a specific style to a syntactic or semantic element, such as a source code token,
 * menu item, or code completion proposal.
 *
 * Often, multiple styles apply to the same element. For example, one style describes the syntactic kind
 * of element, and other styles modify the default style to indicate attributes such as
 * semantic attributes, the element being deprecated, the element being invalid, and so on.
 * The style names of all these attributes are captured in this set.
 */
@Experimental
public final class StyleNames extends SetView<StyleName> implements Serializable {

    private static final StyleNames EMPTY = new StyleNames(Collections.emptySet());

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
        final HashSet<StyleName> set = new HashSet<>();
        set.add(name);
        return new StyleNames(set);
    }

    /**
     * Creates a new instance of the {@link StyleNames} class.
     *
     * Note that duplicate names are removed.
     *
     * @param names the names
     * @return the created {@link StyleName} instance
     */
    public static StyleNames of(StyleName... names) {
        final HashSet<StyleName> set = new HashSet<>();
        Collections.addAll(set, names);
        return new StyleNames(set);
    }

    /**
     * Initializes a new instance of the {@link StyleNames} class.
     *
     * @param set the set of style names
     */
    private StyleNames(Set<StyleName> set) {
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
        if (this.collection.isEmpty()) return false;
        return this.collection.stream().anyMatch(n -> n.startsWith(name));
    }

}
