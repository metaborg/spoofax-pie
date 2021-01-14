package mb.common.token;

import mb.common.region.Region;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class TokenImpl<F> implements Token<F> {
    private final TokenType type;
    private final Region region;
    private final @Nullable F fragment;

    public TokenImpl(TokenType type, Region region, @Nullable F fragment) {
        this.type = type;
        this.region = region;
        this.fragment = fragment;
    }

    @Override public TokenType getType() {
        return type;
    }

    @Override public Region getRegion() {
        return region;
    }

    @Override public @Nullable F getFragment() {
        return fragment;
    }

    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final TokenImpl<?> other = (TokenImpl<?>) obj;
        return type.equals(other.type) &&
            region.equals(other.region) &&
            Objects.equals(fragment, other.fragment);
    }

    @Override public int hashCode() {
        return Objects.hash(type, region, fragment);
    }

    @Override public String toString() {
        return "Token(" + type + ", " + region + ", " + fragment + ")";
    }
}
