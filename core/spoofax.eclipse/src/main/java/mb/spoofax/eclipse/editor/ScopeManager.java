package mb.spoofax.eclipse.editor;

import mb.common.style.Style;
import mb.spoofax.core.platform.PlatformScope;
import mb.spoofax.eclipse.util.ColorShare;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Manages the scope names and their styles.
 */
@PlatformScope
public final class ScopeManager {

    /** The default scope name. */
    public static final String DEFAULT_SCOPE = "text";
    /** The default text attribute; or {@code null} when it has not been created yet. */
    private @Nullable TextAttribute defaultTextAttribute;

    private final ColorShare colorShare;

    @Inject public ScopeManager(ColorShare colorShare) {
        this.colorShare = colorShare;
    }

    /**
     * Gets the styling to use for tokens with the specified scope name.
     *
     * @param scope the scope name
     * @param fallback the fallback style; or {@code null} to specify none
     * @return the scope style
     */
    public TextAttribute getTokenHighlight(String scope, @Nullable Style fallback) {
        if (scope.startsWith(DEFAULT_SCOPE)) {
            return getOrCreateDefault();
        }
        if (fallback != null)
            return createTextAttributeFromStyle(fallback);
        else
            throw new RuntimeException("Not implemented.");
    }

    /**
     * Creates a {@link TextAttribute} that corresponds to the given {@link Style}.
     *
     * @param style the style
     * @return the corresponding text attribute
     */
    private TextAttribute createTextAttributeFromStyle(Style style) {
        final @Nullable Color foreground = fromStyleColor(style.getColor());
        final @Nullable Color background = fromStyleColor(style.getBackgroundColor());
        return createTextAttribute(foreground, background, style.isBold(), style.isItalic(), style.isUnderscore(), style.isStrikeout(), null);
    }

    /**
     * Creates a {@link TextAttribute} with the given parameters.
     *
     * @param foreground the foreground color; or {@code null}
     * @param background the background color; or {@code null}
     * @param bold whether the style is bold
     * @param italic whether the style is italic
     * @param underline whether the style is underlined
     * @param strikethrough whether the style is stricken through
     * @param font the font to use; or {@code null}
     * @return the corresponding text attribute
     */
    private TextAttribute createTextAttribute(
        @Nullable Color foreground, @Nullable Color background,
        boolean bold, boolean italic, boolean underline, boolean strikethrough, @Nullable Font font) {
        int fontStyle = SWT.NORMAL;
        if(bold) { fontStyle |= SWT.BOLD; }
        if(italic) { fontStyle |= SWT.ITALIC; }
        if(underline) { fontStyle |= TextAttribute.UNDERLINE; }
        if(strikethrough) { fontStyle |= TextAttribute.STRIKETHROUGH; }
        return new TextAttribute(foreground, background, fontStyle, font);
    }

    /**
     * Gets or creates the default text attribute.
     *
     * @return the default text attribute
     */
    private TextAttribute getOrCreateDefault() {
        if (defaultTextAttribute != null) return defaultTextAttribute;
        Color foreground = fromRGB(0, 0, 0);    // FIXME: We should not default to black (e.g., dark mode will be unreadable)
        defaultTextAttribute = createTextAttribute(foreground, null, false, false, false, false, null);
        return defaultTextAttribute;
    }

    /**
     * Gets the {@link Color} corresponding to the given {@link mb.common.style.Color}.
     *
     * @param color the style color; or {@code null}
     * @return the SWT color; or {@code null} when the input color is {@code null}
     */
    private @Nullable Color fromStyleColor(mb.common.style.@Nullable Color color) {
        if (color == null) return null;
        return fromRGB(color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Gets the {@link Color} corresponding to the given {@link mb.common.style.Color}.
     *
     * @param red the red color component, between 0 and 255 inclusive
     * @param green the green color component, between 0 and 255 inclusive
     * @param blue the blue color component, between 0 and 255 inclusive
     * @return the SWT color
     */
    private Color fromRGB(int red, int green, int blue) {
        return this.colorShare.getColor(new RGB(red, green, blue));
    }

}
