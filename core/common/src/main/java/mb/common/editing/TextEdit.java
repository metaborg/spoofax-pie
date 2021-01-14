package mb.common.editing;

import mb.common.region.Region;
import mb.common.region.Selection;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

/**
 * An edit to a document's text.
 */
public final class TextEdit implements Serializable {

    private final Region region;
    private final String newText;

    /**
     * Initializes a new instance of the {@link TextEdit} class.
     *
     * @param region the region to replace with the new text; or an offset to only insert text
     * @param newText the new text to insert; or an empty string to delete the existing text
     */
    public TextEdit(Region region, String newText) {
        this.region = region;
        this.newText = newText;
    }

    /**
     * Gets the selection to replace with the new text.
     *
     * @return the selection to replace; or an offset to only insert text
     */
    public Region getRegion() {
        return region;
    }

    /**
     * Gets the new text to insert.
     *
     * @return the new text to insert; or an empty string to delete the existing text
     */
    public String getNewText() {
        return newText;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        TextEdit textEdit = (TextEdit)o;
        // @formatter:off
        return region.equals(textEdit.region)
            && newText.equals(textEdit.newText);
        // @formatter:on
    }

    @Override
    public int hashCode() {
        return Objects.hash(region, newText);
    }

    @Override public String toString() {
        return "edit(" + region + " -> \"" + newText + "\")";
    }
}
