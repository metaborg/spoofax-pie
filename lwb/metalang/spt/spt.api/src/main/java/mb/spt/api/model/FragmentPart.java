package mb.spt.api.model;

import org.checkerframework.checker.nullness.qual.Nullable;

public class FragmentPart {
    public final int startOffset;
    public final String text;

    public FragmentPart(int startOffset, String text) {
        this.startOffset = startOffset;
        this.text = text;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final FragmentPart that = (FragmentPart)o;
        if(startOffset != that.startOffset) return false;
        return text.equals(that.text);
    }

    @Override public int hashCode() {
        int result = startOffset;
        result = 31 * result + text.hashCode();
        return result;
    }

    @Override public String toString() {
        return "FragmentParts{" +
            "startOffset=" + startOffset +
            ", text='" + text + '\'' +
            '}';
    }
}
