package mb.pipe.run.spoofax.esv;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

public class StylingRules implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<SortCons, Style> sortConsToStyle = Maps.newHashMap();
    private final Map<String, Style> consToStyle = Maps.newHashMap();
    private final Map<String, Style> sortToStyle = Maps.newHashMap();
    private final Map<String, Style> tokenToStyle = Maps.newHashMap();


    public boolean hasSortConsStyle(String sort, String cons) {
        return sortConsToStyle.containsKey(new SortCons(sort, cons));
    }

    public boolean hasConsStyle(String cons) {
        return consToStyle.containsKey(cons);
    }

    public boolean hasSortStyle(String sort) {
        return sortToStyle.containsKey(sort);
    }

    public boolean hasTokenStyle(String builtin) {
        return tokenToStyle.containsKey(builtin);
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

    public @Nullable Style tokenStyle(String builtin) {
        return tokenToStyle.get(builtin);
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

    public void mapTokenToStyle(String builtin, Style style) {
        tokenToStyle.put(builtin, style);
    }
}
