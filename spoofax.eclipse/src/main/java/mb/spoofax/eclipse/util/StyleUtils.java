package mb.spoofax.eclipse.util;

import mb.common.style.Style;
import mb.common.style.Styling;
import mb.common.style.TokenStyle;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Utility functions for creating Eclipse text styles.
 */
public final class StyleUtils {
    private final ColorShare colorShare;
    private final Logger logger;


    @Inject public StyleUtils(ColorShare colorShare, LoggerFactory loggerFactory) {
        this.colorShare = colorShare;
        this.logger = loggerFactory.create(getClass());
    }


    public TextPresentation createTextPresentation(mb.common.style.Color color, int length) {
        final TextPresentation presentation = new TextPresentation();
        final StyleRange styleRange = new StyleRange();
        styleRange.start = 0;
        styleRange.length = length;
        styleRange.foreground = createColor(color);
        presentation.addStyleRange(styleRange);
        return presentation;
    }

    public ArrayList<TokenStyle> validateStyling(Styling styling, int length) {
        int offset = -1;
        final ArrayList<TokenStyle> validated = new ArrayList<>();
        for(TokenStyle tokenStyle : styling.stylePerToken()) {
            final mb.common.region.Region region = tokenStyle.token().region();
            if(offset >= region.startOffset) {
                logger.warn("Skipping invalid {}, starting offset is greater than offset in previous regions",
                    tokenStyle);
            } else if(offset >= region.endOffset) {
                logger.warn("Skipping invalid {}, ending offset is greater than offset in previous regions",
                    tokenStyle);
            } else if(region.startOffset > region.endOffset) {
                logger.warn("Skipping invalid {}, starting offset is greater than ending offset", tokenStyle);
            } else if(region.startOffset > length) {
                logger.warn("Skipping invalid {}, starting offset is greater than text length", tokenStyle);
            } else if(region.endOffset >= length) {
                logger.warn("Skipping invalid {}, ending offset is greater than text length", tokenStyle);
            } else {
                validated.add(tokenStyle);
                offset = region.endOffset;
            }
        }
        return validated;
    }

    public TextPresentation createTextPresentation(Styling styling, int length) {
        final ArrayList<TokenStyle> validated = validateStyling(styling, length);
        return createTextPresentation(validated);
    }

    public TextPresentation createTextPresentation(ArrayList<TokenStyle> stylePerToken) {
        final TextPresentation presentation = new TextPresentation();
        for(TokenStyle tokenStyle : stylePerToken) {
            final StyleRange styleRange = createStyleRange(tokenStyle);
            presentation.addStyleRange(styleRange);
        }
        @Nullable IRegion extent = presentation.getExtent();
        if(extent == null) {
            extent = new Region(0, 0);
        }
        final StyleRange defaultStyleRange = new StyleRange();
        defaultStyleRange.start = extent.getOffset();
        defaultStyleRange.length = extent.getLength();
        defaultStyleRange.foreground = createColor(mb.common.style.Color.black);
        presentation.setDefaultStyleRange(defaultStyleRange);

        return presentation;
    }

    public StyleRange createStyleRange(TokenStyle tokenStyle) {
        final Style style = tokenStyle.style();
        final mb.common.region.Region region = tokenStyle.token().region();

        final StyleRange styleRange = new StyleRange();
        final mb.common.style.@Nullable Color foreground = style.color();
        if(foreground != null) {
            styleRange.foreground = createColor(foreground);
        }
        final mb.common.style.@Nullable Color background = style.backgroundColor();
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

        styleRange.start = region.startOffset;
        styleRange.length = region.length();

        return styleRange;
    }

    private Color createColor(mb.common.style.Color color) {
        final RGB rgb = new RGB(color.red(), color.green(), color.blue());
        return colorShare.getColor(rgb);
    }


    /**
     * Creates a deep copy of given style range.
     *
     * @param styleRangeRef Style range to copy.
     * @return Deep copy of given style range.
     */
    public static StyleRange deepCopy(StyleRange styleRangeRef) {
        final StyleRange styleRange = new StyleRange(styleRangeRef);
        styleRange.start = styleRangeRef.start;
        styleRange.length = styleRangeRef.length;
        styleRange.fontStyle = styleRangeRef.fontStyle;
        return styleRange;
    }

    /**
     * Creates deep copies of style ranges in given text presentation.
     *
     * @param presentation Text presentation to copy style ranges of.
     * @return Collection of deep style range copies.
     */
    public static ArrayList<StyleRange> deepCopies(TextPresentation presentation) {
        final ArrayList<StyleRange> styleRanges = new ArrayList<>();
        for(Iterator<StyleRange> iter = presentation.getNonDefaultStyleRangeIterator(); iter.hasNext(); ) {
            final StyleRange styleRange = iter.next();
            styleRanges.add(deepCopy(styleRange));
        }
        return styleRanges;
    }
}
