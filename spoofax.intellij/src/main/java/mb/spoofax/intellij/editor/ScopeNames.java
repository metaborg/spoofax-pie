package mb.spoofax.intellij.editor;

import java.util.Arrays;
import java.util.Objects;


public final class ScopeNames {

    private final String[] scopes;

    public ScopeNames(String... scopes) {
        this.scopes = scopes;
    }

    public String[] getScopes() {
        return scopes;
    }

    public boolean contains(String scopePrefix) {
        return Arrays.stream(scopes).anyMatch(it -> it.startsWith(scopePrefix) );
    }


    @Override
    public boolean equals(Object other) {
        return other instanceof ScopeNames
            && equals((ScopeNames)other);
    }

    public boolean equals(ScopeNames other) {
        return Objects.equals(this.scopes, other.scopes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.scopes);
    }

    @Override
    public String toString() {
        return String.join(", ", this.scopes);
    }

}
