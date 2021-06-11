package mb.spt.api.parse;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class ParseResult implements Serializable {
    public final boolean success;
    public final boolean recovered;
    public final boolean ambiguous;

    public ParseResult(boolean success, boolean recovered, boolean ambiguous) {
        this.success = success;
        this.recovered = recovered;
        this.ambiguous = ambiguous;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ParseResult that = (ParseResult)o;
        if(success != that.success) return false;
        if(recovered != that.recovered) return false;
        return ambiguous == that.ambiguous;
    }

    @Override public int hashCode() {
        int result = (success ? 1 : 0);
        result = 31 * result + (recovered ? 1 : 0);
        result = 31 * result + (ambiguous ? 1 : 0);
        return result;
    }

    @Override public String toString() {
        return "ParseResult{" +
            "success=" + success +
            ", recovered=" + recovered +
            ", ambiguous=" + ambiguous +
            '}';
    }
}
