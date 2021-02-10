package mb.spoofax.eclipse.util;

import mb.spoofax.core.platform.PlatformScope;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;

@PlatformScope
public class ColorShare implements ISharedTextColors {
    private HashMap<RGB, Color> colors = new HashMap<>();

    @Inject public ColorShare() {
    }

    @Override public Color getColor(@NonNull RGB rgb) {
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
}
