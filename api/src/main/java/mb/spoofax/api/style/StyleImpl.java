package mb.spoofax.api.style;

import java.awt.Color;
import java.io.Serializable;

import javax.annotation.Nullable;

public class StyleImpl implements Serializable, Style {
    private static final long serialVersionUID = 1L;

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


    @Override public Color color() {
        return color;
    }

    @Override public Color backgroundColor() {
        return backgroundColor;
    }

    @Override public boolean bold() {
        return bold;
    }

    @Override public boolean italic() {
        return italic;
    }

    @Override public boolean underscore() {
        return underscore;
    }

    @Override public boolean strikeout() {
        return strikeout;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((color == null) ? 0 : color.hashCode());
        result = prime * result + ((backgroundColor == null) ? 0 : backgroundColor.hashCode());
        result = prime * result + (bold ? 1231 : 1237);
        result = prime * result + (italic ? 1231 : 1237);
        result = prime * result + (underscore ? 1231 : 1237);
        result = prime * result + (strikeout ? 1231 : 1237);
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final StyleImpl other = (StyleImpl) obj;
        if(color == null) {
            if(other.color != null)
                return false;
        } else if(!color.equals(other.color))
            return false;
        if(backgroundColor == null) {
            if(other.backgroundColor != null)
                return false;
        } else if(!backgroundColor.equals(other.backgroundColor))
            return false;
        if(bold != other.bold)
            return false;
        if(italic != other.italic)
            return false;
        if(underscore != other.underscore)
            return false;
        return strikeout == other.strikeout;
    }


    @Override public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Style(color: ");
        builder.append(color);
        builder.append(", backgroundColor: ");
        builder.append(backgroundColor);
        builder.append(", bold: ");
        builder.append(bold);
        builder.append(", italic: ");
        builder.append(italic);
        builder.append(", underscore: ");
        builder.append(underscore);
        builder.append(", strikeout: ");
        builder.append(strikeout);
        builder.append(")");
        return builder.toString();
    }
}
