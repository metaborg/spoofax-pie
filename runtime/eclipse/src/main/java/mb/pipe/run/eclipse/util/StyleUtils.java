package mb.pipe.run.eclipse.util;

import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;

import mb.pipe.run.core.model.region.IRegion;
import mb.pipe.run.core.model.style.IStyle;
import mb.pipe.run.core.model.style.IStyling;
import mb.pipe.run.core.model.style.ITokenStyle;

/**
 * Utility functions for creating Eclipse text styles.
 */
public final class StyleUtils {
    private final ISharedTextColors sharedColors;


    public StyleUtils() {
        this.sharedColors = EditorsPlugin.getDefault().getSharedTextColors();
    }


    public TextPresentation createTextPresentation(java.awt.Color color, int length) {
        final TextPresentation presentation = new TextPresentation();
        final StyleRange styleRange = new StyleRange();
        styleRange.start = 0;
        styleRange.length = length;
        styleRange.foreground = createColor(color);
        presentation.addStyleRange(styleRange);
        return presentation;
    }

    public TextPresentation createTextPresentation(IStyling styling) {
        return createTextPresentation(styling.stylePerToken());
    }

    public TextPresentation createTextPresentation(Iterable<ITokenStyle> stylePerToken) {
        final TextPresentation presentation = new TextPresentation();
        for(ITokenStyle tokenStyle : stylePerToken) {
            final StyleRange styleRange = createStyleRange(tokenStyle);
            presentation.addStyleRange(styleRange);
        }
        org.eclipse.jface.text.IRegion extent = presentation.getExtent();
        if(extent == null) {
            extent = new Region(0, 0);
        }
        final StyleRange defaultStyleRange = new StyleRange();
        defaultStyleRange.start = extent.getOffset();
        defaultStyleRange.length = extent.getLength();
        defaultStyleRange.foreground = createColor(java.awt.Color.BLACK);
        presentation.setDefaultStyleRange(defaultStyleRange);

        return presentation;
    }

    public StyleRange createStyleRange(ITokenStyle tokenStyle) {
        final IStyle style = tokenStyle.style();
        final IRegion region = tokenStyle.token().region();

        final StyleRange styleRange = new StyleRange();
        final java.awt.Color foreground = style.color();
        if(foreground != null) {
            styleRange.foreground = createColor(foreground);
        }
        final java.awt.Color background = style.backgroundColor();
        if(background != null) {
            styleRange.background = createColor(background);
        }
        if(style.bold()) {
            styleRange.fontStyle |= SWT.BOLD;
        }
        if(style.italic()) {
            styleRange.fontStyle |= SWT.ITALIC;
        }
        if(style.underscore()) {
            styleRange.underline = true;
        }
        if(style.strikeout()) {
            styleRange.strikeout = true;
        }

        styleRange.start = region.startOffset();
        styleRange.length = region.endOffset() - region.startOffset() + 1;

        return styleRange;
    }

    private Color createColor(java.awt.Color color) {
        final RGB rgb = new RGB(color.getRed(), color.getGreen(), color.getBlue());
        return sharedColors.getColor(rgb);
    }

    private static StyleRange deepCopy(StyleRange styleRangeRef) {
        final StyleRange styleRange = new StyleRange(styleRangeRef);
        styleRange.start = styleRangeRef.start;
        styleRange.length = styleRangeRef.length;
        styleRange.fontStyle = styleRangeRef.fontStyle;
        return styleRange;
    }
}
