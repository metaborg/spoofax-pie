package mb.common.style;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class StyleImpl implements Style {
    private final @Nullable Color color;
    private final @Nullable Color backgroundColor;
    private final boolean bold;
    private final boolean italic;
    private final boolean underscore;
    private final boolean strikeout;


    public StyleImpl(@Nullable Color color, @Nullable Color backgroundColor, boolean bold, boolean italic,
        boolean underscore, boolean strikeout) {
        this.color = color;
        this.backgroundColor = backgroundColor;
        this.bold = bold;
        this.italic = italic;
        this.underscore = underscore;
        this.strikeout = strikeout;
    }


    @Override public @Nullable Color getColor() {
        return color;
    }

    @Override public @Nullable Color getBackgroundColor() {
        return backgroundColor;
    }

    @Override public boolean isBold() {
        return bold;
    }

    @Override public boolean isItalic() {
        return italic;
    }

    @Override public boolean isUnderscore() {
        return underscore;
    }

    @Override public boolean isStrikeout() {
        return strikeout;
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        StyleImpl style = (StyleImpl) o;
        if(bold != style.bold) return false;
        if(italic != style.italic) return false;
        if(underscore != style.underscore) return false;
        if(strikeout != style.strikeout) return false;
        if(!Objects.equals(color, style.color)) return false;
        return Objects.equals(backgroundColor, style.backgroundColor);
    }

    @Override public int hashCode() {
        int result = color != null ? color.hashCode() : 0;
        result = 31 * result + (backgroundColor != null ? backgroundColor.hashCode() : 0);
        result = 31 * result + (bold ? 1 : 0);
        result = 31 * result + (italic ? 1 : 0);
        result = 31 * result + (underscore ? 1 : 0);
        result = 31 * result + (strikeout ? 1 : 0);
        return result;
    }

    @Override public String toString() {
        return "StyleImpl{" +
            "color=" + color +
            ", backgroundColor=" + backgroundColor +
            ", bold=" + bold +
            ", italic=" + italic +
            ", underscore=" + underscore +
            ", strikeout=" + strikeout +
            '}';
    }
}
