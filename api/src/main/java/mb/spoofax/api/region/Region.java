package mb.spoofax.api.region;

import java.io.Serializable;

/**
 * A region represents a finite region in source code text. A region has a start and end offset, represented by
 * the number of characters from the beginning of the source text, with interval [0,#chars). Both the starting and
 * ending numbers are inclusive.
 */
public class Region implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Inclusive starting offset, the number of characters from the beginning of the source text with interval [0,#chars).
     */
    public final int startOffset;
    /**
     * Inclusive ending offset, the number of characters from the beginning of the source text with interval [0,#chars).
     */
    public final int endOffset;


    public Region(int startOffset, int endOffset) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }


    /**
     * @return Length of the region.
     */
    public int length() {
        return (this.endOffset - this.startOffset) + 1;
    }

    /**
     * Checks if this region contains given region.
     *
     * @param region Other region to check.
     * @return True if this region contains given region, false otherwise.
     */
    public boolean contains(Region region) {
        return region.startOffset >= this.startOffset &&
            region.startOffset <= this.endOffset &&
            region.endOffset <= this.endOffset;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + endOffset;
        result = prime * result + startOffset;
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final Region other = (Region) obj;
        if(endOffset != other.endOffset)
            return false;
        return startOffset == other.startOffset;
    }

    @Override public String toString() {
        return startOffset + "-" + endOffset;
    }
}
