package mb.spoofax.api.style;

public class Color {
    private int value;


    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public Color(int r, int g, int b, int a) {
        value = ((a & 0xFF) << 24) |
            ((r & 0xFF) << 16) |
            ((g & 0xFF) << 8) |
            ((b & 0xFF) << 0);
    }

    public Color(int rgb) {
        value = 0xff000000 | rgb;
    }


    public int alpha() {
        return (value >> 24) & 0xff;
    }

    public int red() {
        return (value >> 16) & 0xFF;
    }

    public int green() {
        return (value >> 8) & 0xFF;
    }

    public int blue() {
        return (value >> 0) & 0xFF;
    }

    public int arbg() {
        return value;
    }
}
