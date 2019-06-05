package mb.spoofax.intellij.editor;

import mb.spoofax.intellij.ScopeNames;
import mb.spoofax.intellij.Span;

import java.util.Objects;

public final class Token implements IToken {
    private final Span location;
    private final ScopeNames scopes;


    public Token(Span location, ScopeNames scopes) {
        this.location = location;
        this.scopes = scopes;
    }


    @Override public Span getLocation() {
        return location;
    }

    @Override public ScopeNames getScopes() {
        return scopes;
    }


    @Override public boolean equals(Object other) {
        return other instanceof Token
            && equals((Token) other);
    }

    public boolean equals(Token other) {
        return Objects.equals(this.location, other.location)
            && Objects.equals(this.scopes, other.scopes);
    }

    @Override public int hashCode() {
        return Objects.hash(this.location, this.scopes);
    }

    @Override public String toString() {
        return this.scopes + "@" + this.location;
    }
}
