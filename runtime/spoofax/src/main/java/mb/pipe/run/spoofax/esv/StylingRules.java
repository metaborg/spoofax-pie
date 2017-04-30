package mb.pipe.run.spoofax.esv;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import mb.pipe.run.core.model.parse.ITokenType;
import mb.pipe.run.core.model.style.IStyle;

public class StylingRules implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<SortCons, IStyle> sortConsToStyle = Maps.newHashMap();
    private final Map<String, IStyle> consToStyle = Maps.newHashMap();
    private final Map<String, IStyle> sortToStyle = Maps.newHashMap();
    private final Map<ITokenType, IStyle> tokenTypeToStyle = Maps.newHashMap();


    public boolean hasSortConsStyle(String sort, String cons) {
        return sortConsToStyle.containsKey(new SortCons(sort, cons));
    }

    public boolean hasConsStyle(String cons) {
        return consToStyle.containsKey(cons);
    }

    public boolean hasSortStyle(String sort) {
        return sortToStyle.containsKey(sort);
    }

    public boolean hasTokenTypeStyle(String builtin) {
        return tokenTypeToStyle.containsKey(builtin);
    }


    public @Nullable IStyle sortConsStyle(String sort, String cons) {
        return sortConsToStyle.get(new SortCons(sort, cons));
    }

    public @Nullable IStyle consStyle(String cons) {
        return consToStyle.get(cons);
    }

    public @Nullable IStyle sortStyle(String sort) {
        return sortToStyle.get(sort);
    }

    public @Nullable IStyle tokenTypeStyle(ITokenType type) {
        return tokenTypeToStyle.get(type);
    }


    public void mapSortConsToStyle(String sort, String cons, IStyle style) {
        sortConsToStyle.put(new SortCons(sort, cons), style);
    }

    public void mapConsToStyle(String cons, IStyle style) {
        consToStyle.put(cons, style);
    }

    public void mapSortToStyle(String sort, IStyle style) {
        sortToStyle.put(sort, style);
    }

    public void mapTokenTypeToStyle(ITokenType type, IStyle style) {
        tokenTypeToStyle.put(type, style);
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + consToStyle.hashCode();
        result = prime * result + sortConsToStyle.hashCode();
        result = prime * result + sortToStyle.hashCode();
        result = prime * result + tokenTypeToStyle.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final StylingRules other = (StylingRules) obj;
        if(!consToStyle.equals(other.consToStyle))
            return false;
        if(!sortConsToStyle.equals(other.sortConsToStyle))
            return false;
        if(!sortToStyle.equals(other.sortToStyle))
            return false;
        if(!tokenTypeToStyle.equals(other.tokenTypeToStyle))
            return false;
        return true;
    }
}
