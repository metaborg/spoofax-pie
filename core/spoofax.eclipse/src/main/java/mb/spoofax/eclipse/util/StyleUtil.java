package mb.spoofax.eclipse.util;

import mb.common.style.Styling;
import mb.common.style.TokenStyle;
import mb.common.token.Token;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.spoofax.core.platform.PlatformScope;
import mb.spoofax.eclipse.editor.ScopeManager;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Utility functions for creating Eclipse text styles.
 */
@PlatformScope
public final class StyleUtil {
    private final ColorShare colorShare;
    private final ScopeManager scopeManager;
    private final Logger logger;


    @Inject public StyleUtil(ColorShare colorShare, ScopeManager scopeManager, LoggerFactory loggerFactory) {
        this.colorShare = colorShare;
        this.scopeManager = scopeManager;
        this.logger = loggerFactory.create(getClass());
    }

    public ArrayList<TokenStyle> validateStyling(Styling styling, int length) {
        int offset = -1;
        final ArrayList<TokenStyle> validated = new ArrayList<>();
        for(TokenStyle tokenStyle : styling.getStylePerToken()) {
            final mb.common.region.Region region = tokenStyle.getToken().getRegion();
            if(offset >= region.getStartOffset()) {
                logger.warn("Skipping invalid {}, starting offset is greater than offset in previous regions",
                    tokenStyle);
            } else if(offset >= (region.getEndOffset() - 1)) {
                logger.warn("Skipping invalid {}, ending offset is greater than offset in previous regions",
                    tokenStyle);
            } else if(region.getStartOffset() > (region.getEndOffset() - 1)) {
                logger.warn("Skipping invalid {}, starting offset is greater than ending offset", tokenStyle);
            } else if(region.getStartOffset() > length) {
                logger.warn("Skipping invalid {}, starting offset is greater than text length", tokenStyle);
            } else if((region.getEndOffset() - 1) >= length) {
                logger.warn("Skipping invalid {}, ending offset is greater than text length", tokenStyle);
            } else {
                validated.add(tokenStyle);
                offset = region.getEndOffset() - 1;
            }
        }
        return validated;
    }

    public TextPresentation createDefaultTextPresentation(int length) {
        final TextPresentation presentation = new TextPresentation();
        TextAttribute defaultAttr = scopeManager.getTokenHighlight(ScopeManager.DEFAULT_SCOPE, null);
        final StyleRange styleRange = createStyleRange(defaultAttr, 0, length);
        presentation.addStyleRange(styleRange);
        return presentation;
    }

    public TextPresentation createTextPresentation(Styling styling, int length) {
        final ArrayList<TokenStyle> validated = validateStyling(styling, length);
        return createTextPresentation(validated);
    }

    public TextPresentation createTextPresentation(ArrayList<TokenStyle> stylePerToken) {
        final TextPresentation presentation = new TextPresentation();
        for(TokenStyle tokenStyle : stylePerToken) {
            TextAttribute attr = scopeManager.getTokenHighlight("", tokenStyle.getStyle());
            final StyleRange styleRange = createStyleRange(attr, tokenStyle.getToken());
            presentation.addStyleRange(styleRange);
        }
        @Nullable IRegion extent = presentation.getExtent();
        if(extent == null) {
            extent = new Region(0, 0);
        }
        TextAttribute defaultAttr = scopeManager.getTokenHighlight(ScopeManager.DEFAULT_SCOPE, null);
        final StyleRange defaultStyleRange = createStyleRange(defaultAttr, extent.getOffset(), extent.getLength());
        presentation.setDefaultStyleRange(defaultStyleRange);

        return presentation;
    }

    /**
     * Creates a style range from the given text attribute and token.
     *
     * @param textAttribute the text attribute, which defines the styling
     * @param token         the token
     * @return the {@link StyleRange} with the token's styling
     */
    public StyleRange createStyleRange(TextAttribute textAttribute, Token token) {
        final mb.common.region.Region region = token.getRegion();
        return createStyleRange(textAttribute, region.getStartOffset(), region.getLength());
    }

    /**
     * Creates a style range from the given text attribute and token start offset and length.
     *
     * @param textAttribute the text attribute, which defines the styling
     * @param tokenStart    the offset of the token
     * @param tokenLength   the length of the token
     * @return the {@link StyleRange} with the token's styling
     */
    public StyleRange createStyleRange(TextAttribute textAttribute, int tokenStart, int tokenLength) {
        final int style = textAttribute.getStyle();
        final int fontStyle = style & (SWT.ITALIC | SWT.BOLD | SWT.NORMAL);
        final StyleRange styleRange = new StyleRange(tokenStart, tokenLength,
            textAttribute.getForeground(), textAttribute.getBackground(), fontStyle);
        styleRange.strikeout = (style & TextAttribute.STRIKETHROUGH) != 0;
        styleRange.underline = (style & TextAttribute.UNDERLINE) != 0;
        styleRange.font = textAttribute.getFont();
        return styleRange;
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
