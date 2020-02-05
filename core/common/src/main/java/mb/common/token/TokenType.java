package mb.common.token;

import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class TokenType implements Serializable {
    interface Cases<R> {
        R identifier();

        R string();

        R number();

        R keyword();

        R operator();

        R layout();

        R unknown();
    }

    public abstract <R> R match(Cases<R> cases);

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
