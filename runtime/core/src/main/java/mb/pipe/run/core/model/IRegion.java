package mb.pipe.run.core.model;

import java.io.Serializable;

/**
 * Interface for representing a finite region in source code text. A region has:
 * <ul>
 * <li>Offset - number of characters from the beginning of the source text, with interval [0,#chars).</li>
 * </ul>
 * Both the starting and ending numbers are inclusive.
 */
public interface IRegion extends Serializable {
    /**
     * @return Inclusive starting offset, the number of characters from the beginning of the source text with interval
     *         [0,#chars).
     */
    int startOffset();

    /**
     * @return Inclusive ending offset, the number of characters from the beginning of the source text with interval
     *         [0,#chars).
     */
    int endOffset();


    /**
     * @return Length of the source region.
     */
    default int length() {
        return (this.endOffset() - this.startOffset()) + 1;
    }

    /**
     * Checks if this region contains given region.
     * 
     * @param region
     *            Other region to check.
     * @return True if this region contains given region, false otherwise.
     */
    default boolean contains(IRegion region) {
        return region.startOffset() >= this.startOffset() && region.startOffset() <= this.endOffset()
            && region.endOffset() <= this.endOffset();
    }
}
