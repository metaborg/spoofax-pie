package mb.jsglr1.common;

import mb.common.message.KeyedMessages;
import mb.jsglr.common.JSGLRTokens;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;

public class JSGLR1ParseOutput implements Serializable {
    public final IStrategoTerm ast;
    public final JSGLRTokens tokens;
    public final KeyedMessages messages;
    public final boolean recovered;
    public final String startSymbol;
    public final @Nullable ResourceKey fileHint;
    public final @Nullable ResourcePath rootDirectoryHint;

    public JSGLR1ParseOutput(
        IStrategoTerm ast,
        JSGLRTokens tokens,
        KeyedMessages messages,
        boolean recovered,
        String startSymbol,
        @Nullable ResourceKey fileHint,
        @Nullable ResourcePath rootDirectoryHint
    ) {
        this.ast = ast;
        this.tokens = tokens;
        this.messages = messages;
        this.recovered = recovered;
        this.startSymbol = startSymbol;
        this.fileHint = fileHint;
        this.rootDirectoryHint = rootDirectoryHint;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final JSGLR1ParseOutput that = (JSGLR1ParseOutput)o;
        if(recovered != that.recovered) return false;
        if(!ast.equals(that.ast)) return false;
        if(!tokens.equals(that.tokens)) return false;
        if(!messages.equals(that.messages)) return false;
        if(!startSymbol.equals(that.startSymbol)) return false;
        if(fileHint != null ? !fileHint.equals(that.fileHint) : that.fileHint != null) return false;
        return rootDirectoryHint != null ? rootDirectoryHint.equals(that.rootDirectoryHint) : that.rootDirectoryHint == null;
    }

    @Override public int hashCode() {
        int result = ast.hashCode();
        result = 31 * result + tokens.hashCode();
        result = 31 * result + messages.hashCode();
        result = 31 * result + (recovered ? 1 : 0);
        result = 31 * result + startSymbol.hashCode();
        result = 31 * result + (fileHint != null ? fileHint.hashCode() : 0);
        result = 31 * result + (rootDirectoryHint != null ? rootDirectoryHint.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "JSGLR1ParseOutput{" +
            "ast=" + ast +
            ", tokens=" + tokens +
            ", messages=" + messages +
            ", recovered=" + recovered +
            ", startSymbol='" + startSymbol + '\'' +
            ", fileHint=" + fileHint +
            ", rootDirectoryHint=" + rootDirectoryHint +
            '}';
    }
}
