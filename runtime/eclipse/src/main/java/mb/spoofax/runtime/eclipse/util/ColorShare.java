package mb.spoofax.runtime.eclipse.util;

import java.util.HashMap;

import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorShare implements ISharedTextColors {
    private HashMap<RGB, Color> colors = new HashMap<>();

    @Override public Color getColor(RGB rgb) {
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
