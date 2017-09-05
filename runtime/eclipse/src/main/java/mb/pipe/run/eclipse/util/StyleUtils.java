package mb.pipe.run.eclipse.util;

import java.util.ArrayList;

import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import com.google.inject.Inject;

import mb.log.Logger;
import mb.spoofax.runtime.model.region.Region;
import mb.spoofax.runtime.model.style.Style;
import mb.spoofax.runtime.model.style.Styling;
import mb.spoofax.runtime.model.style.TokenStyle;

/**
 * Utility functions for creating Eclipse text styles.
 */
public final class StyleUtils {
    private final Logger logger;
    private final ColorShare colorShare;


    @Inject public StyleUtils(Logger rootLogger, ColorShare colorShare) {
        this.colorShare = colorShare;
        this.logger = rootLogger.forContext(getClass());
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

    public ArrayList<TokenStyle> validateStyling(Styling styling, int length) {
        int offset = -1;
        final ArrayList<TokenStyle> validated = new ArrayList<>();
        for(TokenStyle tokenStyle : styling.stylePerToken()) {
            final Region region = tokenStyle.token().region();
            if(offset >= region.startOffset()) {
                logger.warn("Skipping invalid {}, starting offset is greater than offset in previous regions",
                    tokenStyle);
            } else if(offset >= region.endOffset()) {
                logger.warn("Skipping invalid {}, ending offset is greater than offset in previous regions",
                    tokenStyle);
            } else if(region.startOffset() > region.endOffset()) {
                logger.warn("Skipping invalid {}, starting offset is greater than ending offset", tokenStyle);
            } else if(region.startOffset() > length) {
                logger.warn("Skipping invalid {}, starting offset is greater than text length", tokenStyle);
            } else if(region.endOffset() >= length) {
                logger.warn("Skipping invalid {}, ending offset is greater than text length", tokenStyle);
            } else {
                validated.add(tokenStyle);
                offset = region.endOffset();
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
        org.eclipse.jface.text.IRegion extent = presentation.getExtent();
        if(extent == null) {
            extent = new org.eclipse.jface.text.Region(0, 0);
        }
        final StyleRange defaultStyleRange = new StyleRange();
        defaultStyleRange.start = extent.getOffset();
        defaultStyleRange.length = extent.getLength();
        defaultStyleRange.foreground = createColor(java.awt.Color.BLACK);
        presentation.setDefaultStyleRange(defaultStyleRange);

        return presentation;
    }

    public StyleRange createStyleRange(TokenStyle tokenStyle) {
        final Style style = tokenStyle.style();
        final Region region = tokenStyle.token().region();

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
        return colorShare.getColor(rgb);
    }

    private static StyleRange deepCopy(StyleRange styleRangeRef) {
        final StyleRange styleRange = new StyleRange(styleRangeRef);
        styleRange.start = styleRangeRef.start;
        styleRange.length = styleRangeRef.length;
        styleRange.fontStyle = styleRangeRef.fontStyle;
        return styleRange;
    }
}
