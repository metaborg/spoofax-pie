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
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Utility functions for creating Eclipse text styles.
 */
@Singleton
public final class StyleUtil {
    private final ColorShare colorShare;
    private final Logger logger;


    @Inject public StyleUtil(ColorShare colorShare, LoggerFactory loggerFactory) {
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
        for(TokenStyle tokenStyle : styling.getStylePerToken()) {
            final mb.common.region.Region region = tokenStyle.getToken().getRegion();
            if(offset >= region.getStartOffset()) {
                logger.warn("Skipping invalid {}, starting offset is greater than offset in previous regions",
                    tokenStyle);
            } else if(offset >= region.getEndOffsetInclusive()) {
                logger.warn("Skipping invalid {}, ending offset is greater than offset in previous regions",
                    tokenStyle);
            } else if(region.getStartOffset() > region.getEndOffsetInclusive()) {
                logger.warn("Skipping invalid {}, starting offset is greater than ending offset", tokenStyle);
            } else if(region.getStartOffset() > length) {
                logger.warn("Skipping invalid {}, starting offset is greater than text length", tokenStyle);
            } else if(region.getEndOffsetInclusive() >= length) {
                logger.warn("Skipping invalid {}, ending offset is greater than text length", tokenStyle);
            } else {
                validated.add(tokenStyle);
                offset = region.getEndOffsetInclusive();
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
        final Style style = tokenStyle.getStyle();
        final mb.common.region.Region region = tokenStyle.getToken().getRegion();

        final StyleRange styleRange = new StyleRange();
        final mb.common.style.@Nullable Color foreground = style.getColor();
        if(foreground != null) {
            styleRange.foreground = createColor(foreground);
        }
        final mb.common.style.@Nullable Color background = style.getBackgroundColor();
        if(background != null) {
            styleRange.background = createColor(background);
        }
        if(style.getIsBold()) {
            styleRange.fontStyle |= SWT.BOLD;
        }
        if(style.getIsItalic()) {
            styleRange.fontStyle |= SWT.ITALIC;
        }
        if(style.getIsUnderscore()) {
            styleRange.underline = true;
        }
        if(style.getIsStrikeout()) {
            styleRange.strikeout = true;
        }

        styleRange.start = region.getStartOffset();
        styleRange.length = region.length();

        return styleRange;
    }

    private Color createColor(mb.common.style.Color color) {
        final RGB rgb = new RGB(color.getRed(), color.getGreen(), color.getBlue());
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
