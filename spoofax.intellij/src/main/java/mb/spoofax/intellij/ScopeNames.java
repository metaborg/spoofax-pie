package mb.spoofax.intellij;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * A set of scope names.
 */
public final class ScopeNames {

    private final List<String> scopes;

    /**
     * Initializes a new instance of the {@link ScopeNames} class
     * from the specified scope names.
     *
     * @param scopes The scope names.
     */
    public ScopeNames(String... scopes) {
        this.scopes = Collections.unmodifiableList(Arrays.asList(scopes));
    }

    /**
     * Gets the list of scope names.
     *
     * The returned list is immutable.
     *
     * @return The list of scope names.
     */
    public List<String> getScopes() {
        return this.scopes;
    }

    /**
     * Determines whether the set of scope names contains a scope with the specified prefix.
     *
     * @param scopePrefix The scope prefix to look for.
     * @return {@code true} when a scope name with the specified prefix was found;
     * otherwise, {@code false}.
     */
    public boolean contains(String scopePrefix) {
        return this.scopes.stream().anyMatch(it -> it.startsWith(scopePrefix + ".") || it.equals(scopePrefix));
    }


    @Override
    public boolean equals(Object other) {
        // @formatter:off
        return other instanceof ScopeNames
            && equals((ScopeNames)other);
        // @formatter:on
    }

    /**
     * Compares this object and the specified object for equality.
     *
     * @param other The object to compare this object to.
     * @return {@code true} when this object is equal to the specified object;
     * otherwise, {@code false}.
     */
    public boolean equals(ScopeNames other) {
        return Objects.equals(this.scopes, other.scopes);
    }

    @Override
    public int hashCode() {
        return this.scopes.hashCode();
    }

    @Override
    public String toString() {
        return String.join(", ", this.scopes);
    }

}
