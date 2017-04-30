package mb.pipe.run.core.model.style;

import java.util.List;

public class Styling implements IStyling {
    private static final long serialVersionUID = 1L;

    private final List<ITokenStyle> stylePerToken;


    public Styling(List<ITokenStyle> stylePerToken) {
        this.stylePerToken = stylePerToken;
    }


    @Override public List<ITokenStyle> stylePerToken() {
        return stylePerToken;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + stylePerToken.hashCode();
        return result;
    }


    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final Styling other = (Styling) obj;
        if(!stylePerToken.equals(other.stylePerToken))
            return false;
        return true;
    }
}
