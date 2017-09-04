package mb.pipe.run.core.model.style;

import java.util.List;

public class StylingImpl implements Styling {
    private static final long serialVersionUID = 1L;

    private final List<TokenStyle> stylePerToken;


    public StylingImpl(List<TokenStyle> stylePerToken) {
        this.stylePerToken = stylePerToken;
    }


    @Override public List<TokenStyle> stylePerToken() {
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
        final StylingImpl other = (StylingImpl) obj;
        if(!stylePerToken.equals(other.stylePerToken))
            return false;
        return true;
    }
}
