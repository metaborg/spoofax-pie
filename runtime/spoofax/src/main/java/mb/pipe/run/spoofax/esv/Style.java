package mb.pipe.run.spoofax.esv;

import java.awt.Color;
import java.io.Serializable;

import javax.annotation.Nullable;

public class Style implements Serializable {
    private static final long serialVersionUID = 1L;

    private final @Nullable Color color;
    private final @Nullable Color backgroundColor;
    private final boolean bold;
    private final boolean italic;
    private final boolean underscore;
    private final boolean strikeout;


    public Style(@Nullable Color color, @Nullable Color backgroundColor, boolean bold, boolean italic,
        boolean underscore, boolean strikeout) {
        this.color = color;
        this.backgroundColor = backgroundColor;
        this.bold = bold;
        this.italic = italic;
        this.underscore = underscore;
        this.strikeout = strikeout;
    }


    public @Nullable Color color() {
        return color;
    }

    public @Nullable Color backgroundColor() {
        return backgroundColor;
    }

    public boolean bold() {
        return bold;
    }

    public boolean italic() {
        return italic;
    }

    public boolean underscore() {
        return underscore;
    }

    public boolean strikeout() {
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
        final Style other = (Style) obj;
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
        if(strikeout != other.strikeout)
            return false;
        return true;
    }

    @Override public String toString() {
        return String.format("Style [color=%s, backgroundColor=%s, bold=%s, italic=%s, underscore=%s, strikeout=%s]",
            color, backgroundColor, bold, italic, underscore, strikeout);
    }
}
