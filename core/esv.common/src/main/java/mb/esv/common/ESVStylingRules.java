package mb.esv.common;

import mb.common.style.Style;
import mb.common.token.TokenType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.io.binary.TermReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;

public class ESVStylingRules implements Serializable {
    private final HashMap<SortCons, Style> sortConsToStyle = new HashMap<>();
    private final HashMap<String, Style> consToStyle = new HashMap<>();
    private final HashMap<String, Style> sortToStyle = new HashMap<>();
    private final HashMap<TokenType, Style> tokenTypeToStyle = new HashMap<>();


    public static ESVStylingRules fromStream(InputStream inputStream) throws IOException {
        final TermReader termReader = new TermReader(new TermFactory());
        final IStrategoTerm esvTerm = termReader.parseFromStream(inputStream);
        return fromESVTerm(esvTerm);
    }

    public static ESVStylingRules fromESVTerm(IStrategoTerm esvTerm) {
        return StylingRulesFromESV.create(esvTerm);
    }


    public boolean hasSortConsStyle(String sort, String cons) {
        return sortConsToStyle.containsKey(new SortCons(sort, cons));
    }

    public boolean hasConsStyle(String cons) {
        return consToStyle.containsKey(cons);
    }

    public boolean hasSortStyle(String sort) {
        return sortToStyle.containsKey(sort);
    }

    public boolean hasTokenTypeStyle(TokenType type) {
        return tokenTypeToStyle.containsKey(type);
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


    void mapSortConsToStyle(String sort, String cons, Style style) {
        sortConsToStyle.put(new SortCons(sort, cons), style);
    }

    void mapConsToStyle(String cons, Style style) {
        consToStyle.put(cons, style);
    }

    void mapSortToStyle(String sort, Style style) {
        sortToStyle.put(sort, style);
    }

    void mapTokenTypeToStyle(TokenType type, Style style) {
        tokenTypeToStyle.put(type, style);
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ESVStylingRules that = (ESVStylingRules) o;
        if(!sortConsToStyle.equals(that.sortConsToStyle)) return false;
        if(!consToStyle.equals(that.consToStyle)) return false;
        if(!sortToStyle.equals(that.sortToStyle)) return false;
        return tokenTypeToStyle.equals(that.tokenTypeToStyle);
    }

    @Override public int hashCode() {
        int result = sortConsToStyle.hashCode();
        result = 31 * result + consToStyle.hashCode();
        result = 31 * result + sortToStyle.hashCode();
        result = 31 * result + tokenTypeToStyle.hashCode();
        return result;
    }

    @Override public String toString() {
        return "StylingRules{" +
            "sortConsToStyle=" + sortConsToStyle +
            ", consToStyle=" + consToStyle +
            ", sortToStyle=" + sortToStyle +
            ", tokenTypeToStyle=" + tokenTypeToStyle +
            '}';
    }
}
