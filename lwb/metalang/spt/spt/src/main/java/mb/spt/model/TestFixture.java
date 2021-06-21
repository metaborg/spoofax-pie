package mb.spt.model;

import org.checkerframework.checker.nullness.qual.Nullable;

public class TestFixture {
    public final String beforeText;
    public final int beforeStartOffset;
    public final String afterText;
    public final int afterStartOffset;

    public TestFixture(String beforeText, int beforeStartOffset, String afterText, int afterStartOffset) {
        this.beforeText = beforeText;
        this.beforeStartOffset = beforeStartOffset;
        this.afterText = afterText;
        this.afterStartOffset = afterStartOffset;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final TestFixture that = (TestFixture)o;
        if(beforeStartOffset != that.beforeStartOffset) return false;
        if(afterStartOffset != that.afterStartOffset) return false;
        if(!beforeText.equals(that.beforeText)) return false;
        return afterText.equals(that.afterText);
    }

    @Override public int hashCode() {
        int result = beforeText.hashCode();
        result = 31 * result + beforeStartOffset;
        result = 31 * result + afterText.hashCode();
        result = 31 * result + afterStartOffset;
        return result;
    }

    @Override public String toString() {
        return "TestFixture{" +
            "beforeText='" + beforeText + '\'' +
            ", beforeStartOffset=" + beforeStartOffset +
            ", afterText='" + afterText + '\'' +
            ", afterStartOffset=" + afterStartOffset +
            '}';
    }
}
