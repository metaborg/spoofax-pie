package mb.common.style;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class Color implements Serializable {
    public final static Color white = new Color(255, 255, 255);
    public final static Color black = new Color(0, 0, 0);

    private final int argb;


    public Color(int r, int g, int b) {
        this(255, r, g, b);
    }

    public Color(int a, int r, int g, int b) {
        if(a < 0 || a > 255 || r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            throw new RuntimeException("Color value out of range");
        }
        this.argb = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
    }


    public int getAlpha() {
        return (argb >> 24) & 0xFF;
    }

    public int getRed() {
        return (argb >> 16) & 0xFF;
    }

    public int getGreen() {
        return (argb >> 8) & 0xFF;
    }

    public int getBlue() {
        return argb & 0xFF;
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Color color = (Color)o;
        return argb == color.argb;
    }

    @Override public int hashCode() {
        return argb;
    }

    @Override public String toString() {
        return "Color(" + getAlpha() + ", " + getRed() + ", " + getGreen() + ", " + getBlue() + ")";
    }
}
