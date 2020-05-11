package mb.common.token;

import mb.common.region.Region;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public interface Token<F> extends Serializable {
    TokenType getType();

    Region getRegion();

    @Nullable F getFragment();
}
