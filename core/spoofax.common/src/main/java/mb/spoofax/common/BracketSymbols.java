package mb.spoofax.common;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class BracketSymbols implements Serializable {
    public final char open;
    public final char close;

    public BracketSymbols(char open, char close) {
        this.open = open;
        this.close = close;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final BracketSymbols that = (BracketSymbols)o;
        if(open != that.open) return false;
        return close == that.close;
    }

    @Override public int hashCode() {
        int result = open;
        result = 31 * result + (int)close;
        return result;
    }

    @Override public String toString() {
        return "BracketSymbol{" +
            "open=" + open +
            ", close=" + close +
            '}';
    }
}
