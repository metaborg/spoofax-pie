package mb.spt.api.parse;

import mb.common.option.Option;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class ExpectedParseResult implements Serializable {
    public final boolean success;
    public final Option<Boolean> recovered;
    public final Option<Boolean> ambiguous;

    public ExpectedParseResult(boolean success, Option<Boolean> recovered, Option<Boolean> ambiguous) {
        this.success = success;
        this.recovered = recovered;
        this.ambiguous = ambiguous;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ExpectedParseResult that = (ExpectedParseResult)o;
        if(success != that.success) return false;
        if(!recovered.equals(that.recovered)) return false;
        return ambiguous.equals(that.ambiguous);
    }

    @Override public int hashCode() {
        int result = (success ? 1 : 0);
        result = 31 * result + recovered.hashCode();
        result = 31 * result + ambiguous.hashCode();
        return result;
    }

    @Override public String toString() {
        return "TestParseResult{" +
            "success=" + success +
            ", recovered=" + recovered +
            ", ambiguous=" + ambiguous +
            '}';
    }
}
