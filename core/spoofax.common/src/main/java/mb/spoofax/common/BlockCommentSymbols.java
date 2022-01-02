package mb.spoofax.common;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class BlockCommentSymbols implements Serializable {
    public final String open;
    public final String close;

    public BlockCommentSymbols(String open, String close) {
        this.open = open;
        this.close = close;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final BlockCommentSymbols that = (BlockCommentSymbols)o;
        if(!open.equals(that.open)) return false;
        return close.equals(that.close);
    }

    @Override public int hashCode() {
        int result = open.hashCode();
        result = 31 * result + close.hashCode();
        return result;
    }

    @Override public String toString() {
        return "BlockCommentSymbols{" +
            "open='" + open + '\'' +
            ", close='" + close + '\'' +
            '}';
    }
}
