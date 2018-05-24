package mb.spoofax.runtime.esv;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import mb.spoofax.api.parse.TokenType;
import mb.spoofax.api.style.Style;

public class StylingRules implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<SortCons, Style> sortConsToStyle = Maps.newHashMap();
    private final Map<String, Style> consToStyle = Maps.newHashMap();
    private final Map<String, Style> sortToStyle = Maps.newHashMap();
    private final Map<TokenType, Style> tokenTypeToStyle = Maps.newHashMap();


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


    public @Nullable Style sortConsStyle(String sort, String cons) {
        return sortConsToStyle.get(new SortCons(sort, cons));
    }

    public @Nullable Style consStyle(String cons) {
        return consToStyle.get(cons);
    }

    public @Nullable Style sortStyle(String sort) {
        return sortToStyle.get(sort);
    }

    public @Nullable Style tokenTypeStyle(TokenType type) {
        return tokenTypeToStyle.get(type);
    }


    public void mapSortConsToStyle(String sort, String cons, Style style) {
        sortConsToStyle.put(new SortCons(sort, cons), style);
    }

    public void mapConsToStyle(String cons, Style style) {
        consToStyle.put(cons, style);
    }

    public void mapSortToStyle(String sort, Style style) {
        sortToStyle.put(sort, style);
    }

    public void mapTokenTypeToStyle(TokenType type, Style style) {
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
        return tokenTypeToStyle.equals(other.tokenTypeToStyle);
    }
}
