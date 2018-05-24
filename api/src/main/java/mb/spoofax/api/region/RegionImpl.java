package mb.spoofax.api.region;

public class RegionImpl implements Region {
    private static final long serialVersionUID = 1L;

    private final int startOffset;
    private final int endOffset;


    public RegionImpl(int startOffset, int endOffset) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }


    @Override public int startOffset() {
        return startOffset;
    }

    @Override public int endOffset() {
        return endOffset;
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
        final RegionImpl other = (RegionImpl) obj;
        if(endOffset != other.endOffset)
            return false;
        return startOffset == other.startOffset;
    }

    @Override public String toString() {
        return startOffset + "-" + endOffset;
    }
}
