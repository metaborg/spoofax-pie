package mb.spoofax.eclipse.util;

import mb.spoofax.core.platform.PlatformScope;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import javax.inject.Inject;
import java.util.HashMap;

import static mb.common.util.FloatUtil.max;
import static mb.common.util.FloatUtil.min;

@PlatformScope
public class ColorShare implements ISharedTextColors {
    private static boolean isDarkTheme = false;

    static {
        calculateDarkTheme();
        PlatformUI.getWorkbench().getThemeManager().addPropertyChangeListener(e -> calculateDarkTheme());
    }

    private static void calculateDarkTheme() {
        final ColorRegistry colorRegistry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
        // Get text color of selected tabs, or ...
        Color foregroundColor = colorRegistry.get("org.eclipse.ui.workbench.ACTIVE_TAB_UNSELECTED_TEXT_COLOR");
        if(foregroundColor == null) // ... the text color of the editors
            foregroundColor = colorRegistry.get("org.eclipse.ui.editors.foregroundColor");
        // If the text color is lighter than average (gray), then it's probably a light theme
        isDarkTheme = foregroundColor.getRed() + foregroundColor.getGreen() + foregroundColor.getBlue() > 384;
    }


    private HashMap<RGB, Color> colors = new HashMap<>();

    @Inject public ColorShare() {
    }

    @Override public Color getColor(@NonNull RGB rgb) {
        if(isDarkTheme) {
            // HACK: invert lightness of colors when Eclipse is using dark theme. From PR:
            //       https://github.com/metaborg/spoofax-eclipse/pull/19/
            rgb = invertLightness(rgb);
        }
        Color color = colors.get(rgb);
        if(color == null) {
            color = new Color(Display.getDefault(), rgb);
            colors.put(rgb, color);
        }
        return color;
    }

    @Override public void dispose() {
        for(Color color : colors.values()) {
            color.dispose();
        }
        colors.clear();
    }


    // Invert lightness of color (note: lightness != brightness/value! L=0 is black, L=1 is white, L=0.5 is color).
    private static RGB invertLightness(RGB color) {
        float[] hsl = rgb2hsl(color.red / 255f, color.green / 255f, color.blue / 255f);
        float t = hsl[2] * hsl[2];
        // Flip lightness using a fancy formula: newL = 0.5 + (1 - L⁴) / 4
        float newL = 0.5f + (1 - t * t) / 4f;
        float[] rgb = hsl2rgb(hsl[0], hsl[1], newL);
        return new RGB((int)(rgb[0] * 255), (int)(rgb[1] * 255), (int)(rgb[2] * 255));
    }

    // https://stackoverflow.com/a/54071699
    // input: r,g,b in [0,1], output: h in [0,360) and s,l in [0,1]
    private static float[] rgb2hsl(float r, float g, float b) {
        float a = max(r, g, b), n = a - min(r, g, b), f = (1 - Math.abs(a + a - n - 1));
        float h = n == 0 ? 0 : ((a == r) ? (g - b) / n : ((a == g) ? 2 + (b - r) / n : 4 + (r - g) / n));
        return new float[]{60 * (h < 0 ? h + 6 : h), f == 0 ? 0 : n / f, (a + a - n) / 2};
    }

    // https://stackoverflow.com/a/54014428
    // input: h in [0,360] and s,l in [0,1] - output: r,g,b in [0,1]
    private static float[] hsl2rgb(float h, float s, float l) {
        float a = s * min(l, 1 - l);
        float[] c = new float[3];
        for(int i = 0; i < 3; i++) {
            int n = (12 - i * 4) % 12;
            float k = (n + h / 30) % 12;
            c[i] = l - a * max(min(k - 3, 9 - k, 1), -1);
        }
        return c;
    }
}
