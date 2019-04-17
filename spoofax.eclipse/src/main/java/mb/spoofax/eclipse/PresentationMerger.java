package mb.spoofax.eclipse;

import mb.spoofax.eclipse.util.StyleUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.custom.StyleRange;

import java.util.ArrayList;

public class PresentationMerger implements ITextPresentationListener {
    private volatile @Nullable TextPresentation sourcePresentation = null;
    private volatile ArrayList<StyleRange> styleRanges = new ArrayList<>();


    public void set(TextPresentation presentation) {
        sourcePresentation = presentation;
        // Make a deep copy of style ranges to prevent sharing with other ITextPresentationListeners.
        styleRanges = StyleUtils.deepCopies(presentation);
    }

    public void invalidate() {
        sourcePresentation = null;
        styleRanges = new ArrayList<>();
    }


    @Override public void applyTextPresentation(@NonNull TextPresentation targetPresentation) {
        // No need to apply text presentation if source and target presentation are the same object.
        if(sourcePresentation == null || targetPresentation == sourcePresentation) {
            return;
        }

        final IRegion extent = targetPresentation.getExtent();
        final int min = extent.getOffset();
        final int max = min + extent.getLength();
        for(StyleRange styleRange : styleRanges) {
            final int styleRangeEnd = styleRange.start + styleRange.length;
            // Not allowed to change style ranges outside of extent. Safe to skip since they will not be redrawn.
            if(styleRange.start < min || styleRangeEnd > max) {
                continue;
            }
            targetPresentation.mergeStyleRange(styleRange);
        }
    }
}
