package mb.common.token;

import mb.common.region.Region;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class TokenImpl<F> implements Token<F> {
    private final Region region;
    private final TokenType type;
    private final @Nullable F fragment;

    public TokenImpl(Region region, TokenType type, @Nullable F fragment) {
        this.region = region;
        this.type = type;
        this.fragment = fragment;
    }

    @Override public Region getRegion() {
        return region;
    }

    @Override public TokenType getType() {
        return type;
    }

    @Override public @Nullable F getFragment() {
        return fragment;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final TokenImpl token = (TokenImpl) o;
        if(!region.equals(token.region)) return false;
        if(!type.equals(token.type)) return false;
        return Objects.equals(fragment, token.fragment);
    }

    @Override public int hashCode() {
        int result = region.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (fragment != null ? fragment.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "Token(region: " + region + ", type: " + type + ")";
    }
}
