package mb.pipe.run.core.model;

public class Region implements IRegion {
    private static final long serialVersionUID = 1L;

    private final int startOffset;
    private final int endOffset;


    public Region(int startOffset, int endOffset) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }


    @Override public int startOffset() {
        return startOffset;
    }

    @Override public int endOffset() {
        return endOffset;
    }
}
